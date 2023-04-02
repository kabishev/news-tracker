package newstracker

import cats.effect.IO
import cats.effect.IOApp
import fs2.Stream
import newstracker.config.ApplicationConfig
import newstracker.http.HttpApi
import org.typelevel.log4cats.slf4j.Slf4jLogger
import newstracker.article.Articles

import newstracker.http.HttpServer
object Application extends IOApp.Simple {
  implicit val logger = Slf4jLogger.getLogger[IO]
  override def run: IO[Unit] = for {
    config <- ApplicationConfig.load[IO]
    _      <- logger.info(Console.GREEN + config + Console.RESET)
    _ <- ApplicationResources.make[IO](config).use { resources =>
      for {
        articles <- Articles.make[IO](resources)
        http     <- HttpApi.make[IO](articles)
        server = HttpServer[IO].ember(config.httpServer, http.app).use(_ => IO.never)
        _ <- Stream.eval(server).compile.drain
      } yield ()
    }
  } yield ()
}
