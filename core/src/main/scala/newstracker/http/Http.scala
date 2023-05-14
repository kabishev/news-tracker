package newstracker.http

import cats.effect.Async
import cats.syntax.semigroupk._
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.middleware._
import org.http4s.server.websocket.WebSocketBuilder2

import newstracker.article.Articles
import newstracker.health.Health
import newstracker.translation.Translations
import newstracker.ws.Ws

import scala.concurrent.duration._

final class Http[F[_]: Async] private (
    private val health: Health[F],
    private val ws: Ws[F],
    private val articles: Articles[F],
    private val translations: Translations[F]
) {
  private def routes: WebSocketBuilder2[F] => HttpRoutes[F] = wsb => {
    val api = articles.controller.routes(wsb) <+> translations.controller.routes(wsb)
    Router(
      "/api"    -> api,
      "/ws"     -> ws.controller.routes(wsb),
      "/health" -> health.controller.routes(wsb)
    )
  }

  private val middleware: HttpRoutes[F] => HttpRoutes[F] = ((http: HttpRoutes[F]) => AutoSlash(http))
    .andThen((http: HttpRoutes[F]) => CORS.policy.withAllowOriginAll.withAllowCredentials(false).apply(http))
    .andThen((http: HttpRoutes[F]) => Timeout(60.seconds)(http))

  private val loggers: HttpApp[F] => HttpApp[F] = { (http: HttpApp[F]) =>
    RequestLogger.httpApp(
      logHeaders = true,
      logBody = true
    )(http)
  }
    .andThen((http: HttpApp[F]) => ResponseLogger.httpApp(true, true)(http))

  val app: WebSocketBuilder2[F] => HttpApp[F] = wsb => loggers(middleware(routes(wsb)).orNotFound)
}

object Http {
  def make[F[_]: Async](
      health: Health[F],
      ws: Ws[F],
      articles: Articles[F],
      translations: Translations[F]
  ): F[Http[F]] = Async[F].pure(new Http(health, ws, articles, translations))
}
