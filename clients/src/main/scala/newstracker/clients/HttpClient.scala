package newstracker.clients

import cats.effect.Temporal
import cats.syntax.applicativeError._
import cats.syntax.apply._
import org.typelevel.log4cats.Logger
import sttp.client3._

import scala.concurrent.duration._

trait HttpClient[F[_]] {
  protected val name: String
  protected val httpBackend: SttpBackend[F, Any]

  protected val delayBetweenFailures: FiniteDuration = 10.seconds

  protected def dispatch[T](request: Request[T, Any])(implicit F: Temporal[F], logger: Logger[F]): F[Response[T]] =
    dispatchWithRetry(httpBackend, request)

  private def dispatchWithRetry[T](
      backend: SttpBackend[F, Any],
      request: Request[T, Any],
      attempt: Int = 0,
      maxRetries: Int = 10
  )(implicit F: Temporal[F], logger: Logger[F]): F[Response[T]] =
    backend
      .send(request)
      .handleErrorWith { error =>
        if (attempt < maxRetries) {
          val cause      = Option(error.getCause)
          val errorClass = cause.fold(error.getClass.getSimpleName)(_.getClass.getSimpleName)
          val errorMsg   = cause.fold(error.getMessage)(_.getMessage)

          logger.error(s"$name-client/${errorClass.toLowerCase}-$attempt: ${errorMsg}\n$error") *>
            F.sleep(delayBetweenFailures) *>
            dispatchWithRetry(backend, request, attempt + 1, maxRetries)
        } else F.raiseError(error)
      }
}
