package newstracker.health

import cats.effect.{Async, Temporal}
import cats.syntax.functor._
import io.circe.generic.auto._
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter

import newstracker.common.Controller

import java.time.Instant

final class HealthController[F[_]: Async](private val startupTime: Instant) extends Controller[F] {

  private val statusEndpoint: ServerEndpoint[Any, F] = infallibleEndpoint.get
    .in("status")
    .out(jsonBody[HealthController.AppStatus])
    .serverLogicSuccess(_ => Async[F].pure(HealthController.AppStatus(startupTime)))

  override def routes = _ => Http4sServerInterpreter[F]().toRoutes(statusEndpoint)
}

object HealthController {

  final case class AppStatus(startupTime: Instant)

  def make[F[_]: Async]: F[Controller[F]] =
    Temporal[F].realTimeInstant
      .map(ref => new HealthController[F](ref))
}
