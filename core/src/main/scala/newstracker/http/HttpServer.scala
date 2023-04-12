package newstracker.http

import cats.effect._
import com.comcast.ip4s._
import org.http4s.HttpApp
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server._
import org.typelevel.log4cats.Logger

import newstracker.config.HttpServerConfig

trait HttpServer[F[_]] {
  def ember(cfg: HttpServerConfig, httpApp: HttpApp[F]): Resource[F, Server]
}

object HttpServer {
  def apply[F[_]: Async: Logger]: HttpServer[F] = new HttpServer[F] {
    private def showBanner(s: Server): F[Unit] =
      Logger[F].info(s"\n${defaults.Banner.mkString("\n")}\nServer started at ${s.address}")

    override def ember(cfg: HttpServerConfig, httpApp: HttpApp[F]): Resource[F, Server] =
      EmberServerBuilder
        .default[F]
        .withHostOption(Ipv4Address.fromString(cfg.host))
        .withPort(Port.fromInt(cfg.port).get)
        .withHttpApp(httpApp)
        .build
        .evalTap(showBanner)
  }
}
