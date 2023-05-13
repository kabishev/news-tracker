package newstracker.translation.deepl

import cats.effect._
import cats.implicits._
import org.typelevel.log4cats.Logger
import retry.RetryPolicies._
import retry._
import sttp.client3._
import sttp.client3.circe.asJson

import newstracker.ApplicationResources
import newstracker.translation.deepl.responses._

import scala.concurrent.duration._

trait DeeplClient[F[_]] {
  def translate(source: String, sourceLanguage: String, targetLanguage: String): F[String]
}

final private[deepl] class LiveDeeplClient[F[_]: Async: Logger](
    config: DeeplConfig,
    backend: SttpBackend[F, Any]
) extends DeeplClient[F] {
  private val headers: Map[String, String] = Map(
    "Authorization" -> s"DeepL-Auth-Key ${config.authKey}"
  )

  override def translate(source: String, sourceLanguage: String, targetLanguage: String): F[String] =
    retryingOnAllErrors[String](policy, logError) {
      basicRequest
        .post(uri"${config.baseUri}/v2/translate")
        .headers(headers)
        .multipartBody(
          multipart("text", source),
          multipart("source_lang", sourceLanguage),
          multipart("target_lang", targetLanguage),
          multipart("tag_handling", "html")
        )
        .response(asJson[Translate.Response])
        .send(backend)
        .flatMap(_.body match {
          case Right(response) => response.translations.head.text.pure[F]
          case Left(error) =>
            Logger[F].error(s"traslate request failed: ${error}") *>
              error.raiseError[F, String]
        })
    }

  private val policy = limitRetries[F](5).join(exponentialBackoff[F](10.milliseconds))

  private def logError(err: Throwable, details: RetryDetails): F[Unit] = details match {
    case r: RetryDetails.WillDelayAndRetry =>
      Logger[F].error(s"Request failed. Retry after ${r.nextDelay}") *> Logger[F].error(s"Error: ${err.getMessage}")
    case g: RetryDetails.GivingUp =>
      Logger[F].error(s"Giving up after ${g.totalRetries} retries") *> Logger[F].error(s"Error: ${err.getMessage}")
  }

}

object DeeplClient {
  def make[F[_]: Async: Logger](
      config: DeeplConfig,
      resources: ApplicationResources[F]
  ): F[DeeplClient[F]] = Async[F].pure(new LiveDeeplClient[F](config, resources.httpClientBackend))
}
