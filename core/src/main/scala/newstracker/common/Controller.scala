package newstracker.common

import cats.MonadThrow
import cats.implicits._
import io.circe.generic.auto._
import org.http4s._
import sttp.model.StatusCode
import sttp.tapir.DecodeResult.Error._
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._

import newstracker.ApplicationError

final case class ErrorResponse(message: String)

trait Controller[F[_]] {
  implicit class ResponseOps[A](fa: F[A])(implicit F: MonadThrow[F]) {
    def voidResponse: F[Either[(StatusCode, ErrorResponse), Unit]] = mapResponse(_ => ())
    def mapResponse[B](fab: A => B): F[Either[(StatusCode, ErrorResponse), B]] =
      fa.map(fab(_).asRight[(StatusCode, ErrorResponse)])
        .handleError(e => Controller.mapError(e).asLeft[B])
  }

  def routes: HttpRoutes[F]
}

object Controller {

  private val error = statusCode.and(jsonBody[ErrorResponse])

  val publicEndpoint: PublicEndpoint[Unit, (StatusCode, ErrorResponse), Unit, Any] =
    endpoint.errorOut(error)

  private val FailedRegexValidation  = "Predicate failed: \"(.*)\"\\.matches\\(.*\\)\\.".r
  private val MissingFieldValidation = "Missing required field".r
  private val EmptyFieldValidation   = "Predicate isEmpty\\(\\) did not fail\\.".r
  private val IdValidation           = "Predicate failed: \\((.*) is valid id\\).".r

  private def formatJsonError(err: JsonDecodeException): String = err.errors
    .map {
      case JsonError(FailedRegexValidation(value), path) => s"$value is not a valid ${path.head.name}"
      case JsonError(MissingFieldValidation(), path)     => s"Missing required field ${path.head.name}"
      case JsonError(EmptyFieldValidation(), path)       => s"Field ${path.head.name} cannot be empty"
      case JsonError(IdValidation(value), path)          => s"$value is not a valid ${path.head.name}"
      case JsonError(msg, path) if path.isEmpty          => s"Invalid ${path.head.name}: $msg"
      case JsonError(msg, _)                             => msg
    }
    .mkString(", ")

  def mapError(error: Throwable): (StatusCode, ErrorResponse) = error match {
    case e: ApplicationError.Conflict   => (StatusCode.Conflict, ErrorResponse(e.getMessage))
    case e: ApplicationError.NotFound   => (StatusCode.NotFound, ErrorResponse(e.getMessage))
    case e: ApplicationError.BadRequest => (StatusCode.BadRequest, ErrorResponse(e.getMessage))
    case e: ApplicationError.Forbidden  => (StatusCode.Forbidden, ErrorResponse(e.getMessage))
    case e: JsonDecodeException         => (StatusCode.UnprocessableEntity, ErrorResponse(formatJsonError(e)))
    case e                              => (StatusCode.InternalServerError, ErrorResponse(e.getMessage))
  }
}
