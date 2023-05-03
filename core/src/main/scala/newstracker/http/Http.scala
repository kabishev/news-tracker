package newstracker.http

import cats.Monad
import cats.effect.Async
import cats.syntax.semigroupk._
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.middleware._
import org.http4s.server.websocket.WebSocketBuilder2

import newstracker.article.Articles
import newstracker.health.Health

import scala.concurrent.duration._

final class Http[F[_]: Async] private (
    private val health: Health[F],
    private val articles: Articles[F]
) {
  private def webSocketRoutes: WebSocketBuilder2[F] => HttpRoutes[F] = wsb => {
    val api = articles.controller.webSocketRoutes(wsb) <+> articles.controller.routes
    Router(
      "/api" -> api,
      "/"    -> health.controller.routes
    )
  }

  private val middleware: HttpRoutes[F] => HttpRoutes[F] = ((http: HttpRoutes[F]) => AutoSlash(http))
    .andThen((http: HttpRoutes[F]) => CORS.policy.withAllowOriginAll.withAllowCredentials(false).apply(http))
    .andThen((http: HttpRoutes[F]) => Timeout(60.seconds)(http))

  private val loggers: HttpApp[F] => HttpApp[F] = { (http: HttpApp[F]) => RequestLogger.httpApp(true, true)(http) }
    .andThen((http: HttpApp[F]) => ResponseLogger.httpApp(true, true)(http))

  val app: WebSocketBuilder2[F] => HttpApp[F] = wsb => loggers(middleware(webSocketRoutes(wsb)).orNotFound)
}

object Http {
  def make[F[_]: Async](
      health: Health[F],
      articles: Articles[F]
  ): F[Http[F]] = Monad[F].pure(new Http(health, articles))
}
