package newstracker.health

import cats.effect.{Async, Ref, Temporal}
import cats.syntax.flatMap._
import cats.syntax.functor._
import io.circe.Codec
import io.circe.generic.auto._
import io.estatico.newtype.macros.newtype
import org.http4s.HttpRoutes
import org.http4s.server.websocket.WebSocketBuilder2
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter

import newstracker.common.Controller

import java.time.Instant

final class HealthController[F[_]: Async](private val startupTime: Ref[F, Instant]) extends Controller[F] {

  private val statusEndpoint: ServerEndpoint[Any, F] = infallibleEndpoint.get
    .in("health" / "status")
    .out(jsonBody[HealthController.AppStatus])
    .serverLogicSuccess(req => startupTime.get.map(t => HealthController.AppStatus(t)))

  override def routes: HttpRoutes[F]                                  = Http4sServerInterpreter[F]().toRoutes(statusEndpoint)
  override def webSocketRoutes: WebSocketBuilder2[F] => HttpRoutes[F] = _ => HttpRoutes.empty
}

object HealthController {

  final case class AppStatus(startupTime: Instant)

  def make[F[_]: Async]: F[Controller[F]] =
    Temporal[F].realTimeInstant
      .flatMap(ts => Ref.of(ts))
      .map(ref => new HealthController[F](ref))
}
