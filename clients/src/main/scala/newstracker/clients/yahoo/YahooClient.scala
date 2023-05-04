package newstracker.clients.yahoo

import cats.data.NonEmptyList
import cats.effect._
import cats.implicits._
import fs2.Stream
import org.typelevel.log4cats.Logger
import sttp.client3._
import sttp.client3.circe.asJson
import sttp.model._

import newstracker.clients.ApplicationResources
import newstracker.clients.common.SearchArticleClient
import newstracker.clients.yahoo.db.ArticleRepository
import newstracker.clients.yahoo.domain._
import newstracker.clients.yahoo.responses._
import newstracker.kafka._

import java.time.LocalDate

final private[yahoo] class LiveYahooRapidClient[F[_]: Async: Concurrent: Logger](
    config: YahooConfig,
    backend: SttpBackend[F, Any],
    service: ArticleService[F],
    createArticleProducer: Producer[F, Unit, createArticle.Event]
) extends SearchArticleClient[F] {
  import YahooClient._

  private val headers: Map[String, String] = Map(
    "X-RapidAPI-Host" -> config.apiHost,
    "X-RapidAPI-Key"  -> config.apiKey
  )

  override def search(): F[Unit] =
    Stream
      .awakeEvery(config.pollInterval)
      .evalMap { _ =>
        for {
          newArticleIds <- getNewArticleIds()
          storedUuids   <- service.getAll.take(100).compile.to(Set)
        } yield NonEmptyList.fromList(newArticleIds.filterNot(storedUuids.contains))
      }
      .unNone
      .evalTap(uuids => service.create(uuids.map(uuid => CreateArticle(uuid, ArticleCreatedAt(LocalDate.now())))))
      .flatMap(uuids => Stream.emits(uuids.toList))
      .evalMap(getArticleDetails(_).map(details => ((), details.toEvent)))
      .through(createArticleProducer.pipe)
      .compile
      .drain
      .handleErrorWith(error => Logger[F].error(s"YahooRapidClient search failed: ${error.getMessage}").as(()))

  private def getNewArticleIds(): F[List[ArticleUuid]] =
    basicRequest
      .post(uri"${config.baseUri}/news/v2/list?region=$region")
      .header(HeaderNames.ContentType, MediaType.TextPlain.toString())
      .headers(headers)
      .response(asJson[NewsList.Response])
      .send(backend)
      .flatMap(_.body match {
        case Right(body) => body.data.main.stream.map(item => ArticleUuid(item.id)).pure[F]
        case Left(error) =>
          Logger[F].error(s"getNewArticleIds request failed: ${error.getMessage}") *>
            error.raiseError[F, List[ArticleUuid]]
      })

  private def getArticleDetails(articleId: ArticleUuid): F[ArticleDetails] =
    basicRequest
      .get(uri"${config.baseUri}/news/v2/get-details?uuid=${articleId.value}&region=$region")
      .headers(headers)
      .response(asJson[NewsGetDetails.Response])
      .send(backend)
      .flatMap(_.body match {
        case Right(body) => body.toArticleDetails.pure[F]
        case Left(error) =>
          Logger[F].error(s"getArticleDetails request failed: ${error.getMessage}") *>
            error.raiseError[F, ArticleDetails]
      })
}

object YahooClient {
  val region = "de"

  def make[F[_]: Async: Logger](
      config: YahooConfig,
      resources: ApplicationResources[F]
  ): F[SearchArticleClient[F]] =
    for {
      repository <- ArticleRepository.make[F](resources.mongo)
      service    <- ArticleService.make[F](repository)
    } yield new LiveYahooRapidClient[F](
      config,
      resources.httpClientBackend,
      service,
      resources.createArticleProducer
    )

  implicit class ArticleDetailsOps(val details: ArticleDetails) extends AnyVal {
    def toEvent = createArticle.Event(
      details.title.value,
      details.content.value,
      details.createdAt.value,
      region,
      details.authors.value,
      details.summary.value.some,
      details.url.value.some,
      details.source.value.some
    )
  }
}
