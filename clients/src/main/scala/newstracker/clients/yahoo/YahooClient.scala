package newstracker.clients.yahoo

import cats.effect._
import cats.implicits._
import org.typelevel.log4cats.Logger
import retry.RetryPolicies._
import retry._
import sttp.client3._
import sttp.client3.circe.asJson
import sttp.model._

import newstracker.clients.ApplicationResources
import newstracker.clients.yahoo.domain._
import newstracker.clients.yahoo.responses._

import scala.concurrent.duration._

trait YahooClient[F[_]] {
  def getNewArticleIds(): F[List[ArticleUuid]]
  def getArticleDetails(articleId: ArticleUuid): F[ArticleDetails]
}

final private[yahoo] class LiveYahooRapidClient[F[_]: Async: Logger](
    config: YahooConfig,
    backend: SttpBackend[F, Any]
) extends YahooClient[F] {
  import YahooClient._

  private val headers: Map[String, String] = Map(
    "X-RapidAPI-Host" -> config.apiHost,
    "X-RapidAPI-Key"  -> config.apiKey
  )

  private val policy = limitRetries[F](5).join(exponentialBackoff[F](10.milliseconds))

  private def logError(err: Throwable, details: RetryDetails): F[Unit] = details match {
    case r: RetryDetails.WillDelayAndRetry =>
      Logger[F].error(s"Request failed. Retry after ${r.nextDelay}") *> Logger[F].error(s"Error: ${err.getMessage}")
    case g: RetryDetails.GivingUp =>
      Logger[F].error(s"Giving up after ${g.totalRetries} retries") *> Logger[F].error(s"Error: ${err.getMessage}")
  }

  override def getNewArticleIds(): F[List[ArticleUuid]] =
    retryingOnAllErrors[List[ArticleUuid]](policy, logError) {
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
    }

  override def getArticleDetails(articleId: ArticleUuid): F[ArticleDetails] =
    retryingOnAllErrors[ArticleDetails](policy, logError) {
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
}

object YahooClient {
  val region = "de"

  def make[F[_]: Async: Logger](
      config: YahooConfig,
      resources: ApplicationResources[F]
  ): F[YahooClient[F]] = Async[F].pure(new LiveYahooRapidClient[F](config, resources.httpClientBackend))
}
