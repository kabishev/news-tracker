package newstracker

import cats.effect.{IO, IOApp}
import fs2.Stream
import org.typelevel.log4cats.slf4j.Slf4jLogger

import newstracker.article.Articles
import newstracker.config.ApplicationConfig
import newstracker.http.{HttpApi, HttpServer}

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
        kafka = articles.kafka
        _ <- Stream.eval(server).concurrently(kafka.stream).compile.drain
      } yield ()
    }
  } yield ()
}
