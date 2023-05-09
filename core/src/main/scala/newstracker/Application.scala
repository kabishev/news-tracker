package newstracker

import cats.effect.{IO, IOApp}
import fs2.Stream
import org.typelevel.log4cats.slf4j.Slf4jLogger

import newstracker.article.Articles
import newstracker.config.ApplicationConfig
import newstracker.health.Health
import newstracker.http._

object Application extends IOApp.Simple {
  implicit val logger = Slf4jLogger.getLogger[IO]
  override def run: IO[Unit] = for {
    config <- ApplicationConfig.load[IO]
    _      <- logger.info(Console.GREEN + config + Console.RESET)
    _ <- ApplicationResources.make[IO](config).use { resources =>
      for {
        health   <- Health.make[IO]
        articles <- Articles.make[IO](resources)
        http     <- Http.make[IO](health, articles)
        server = HttpServer[IO].ember(config.httpServer, http).use(_ => IO.never)
        kafka  = articles.kafka
        _ <- Stream.eval(server).concurrently(kafka.stream).compile.drain
      } yield ()
    }
  } yield ()
}
