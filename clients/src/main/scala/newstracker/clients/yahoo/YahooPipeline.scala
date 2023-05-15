package newstracker.clients.yahoo

import cats.data.NonEmptyList
import cats.effect._
import cats.implicits._
import fs2.Stream
import org.typelevel.log4cats.Logger

import newstracker.clients.ApplicationResources
import newstracker.clients.common.SearchPipeline
import newstracker.clients.yahoo.db.ArticleRepository
import newstracker.clients.yahoo.domain._
import newstracker.kafka.Producer
import newstracker.kafka.command.CreateArticleCommand

import java.time.LocalDate

final private[yahoo] class LiveYahooPipeline[F[_]: Async: Logger](
    config: YahooConfig,
    client: YahooClient[F],
    service: ArticleService[F],
    createArticleProducer: Producer[F, Unit, CreateArticleCommand]
) extends SearchPipeline[F] {
  import YahooSearchPipeline._

  override def search(): F[Unit] = {
    def searchStream: Stream[F, Unit] = Stream
      .awakeEvery(config.pollInterval)
      .evalMap { _ =>
        for {
          newArticleIds <- client.getNewArticleIds()
          storedUuids   <- service.getAll.take(100).compile.to(Set)
        } yield NonEmptyList.fromList(newArticleIds.filterNot(storedUuids.contains))
      }
      .unNone
      .evalTap(uuids => service.create(uuids.map(uuid => CreateArticle(uuid, ArticleCreatedAt(LocalDate.now())))))
      .flatMap(uuids => Stream.emits(uuids.toList))
      .evalMap(client.getArticleDetails(_).map(details => ((), details.toCreateArticleCommand)))
      .through(createArticleProducer.pipe)
      .handleErrorWith(error => Stream.eval(Logger[F].error(s"search failed: ${error.getMessage}")) >> searchStream)

    searchStream.compile.drain
  }
}

object YahooSearchPipeline {
  val region = "de"

  def make[F[_]: Async: Logger](
      config: YahooConfig,
      resources: ApplicationResources[F]
  ): F[SearchPipeline[F]] =
    for {
      client     <- YahooClient.make[F](config, resources)
      repository <- ArticleRepository.make[F](resources.mongo)
      service    <- ArticleService.make[F](repository)
    } yield new LiveYahooPipeline[F](config, client, service, resources.createArticleProducer)

  implicit class ArticleDetailsOps(val details: ArticleDetails) extends AnyVal {
    def toCreateArticleCommand = CreateArticleCommand(
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
