package newstracker.http

import cats.Monad
import cats.effect.Async
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.middleware._

import scala.concurrent.duration._
import newstracker.article.Articles

final class HttpApi[F[_]: Async] private (private val articles: Articles[F]) {
  private val routes: HttpRoutes[F] = Router(
    "/api" -> articles.controller.routes
  )

  private val middleware: HttpRoutes[F] => HttpRoutes[F] = ((http: HttpRoutes[F]) => AutoSlash(http))
    .andThen((http: HttpRoutes[F]) => CORS.policy.withAllowOriginAll.withAllowCredentials(false).apply(http))
    .andThen((http: HttpRoutes[F]) => Timeout(60.seconds)(http))

  private val loggers: HttpApp[F] => HttpApp[F] = { (http: HttpApp[F]) => RequestLogger.httpApp(true, true)(http) }
    .andThen((http: HttpApp[F]) => ResponseLogger.httpApp(true, true)(http))

  val app: HttpApp[F] = loggers(middleware(routes).orNotFound)
}

object HttpApi {
  def make[F[_]: Async](articles: Articles[F]): F[HttpApi[F]] = Monad[F].pure(new HttpApi(articles))
}
