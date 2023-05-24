package newstracker

import cats.effect.{IO, IOApp}
import fs2.Stream
import org.typelevel.log4cats.slf4j.Slf4jLogger

import newstracker.article.Articles
import newstracker.config.ApplicationConfig
import newstracker.health.Health
import newstracker.http._
import newstracker.translation.Translations
import newstracker.ws.Ws

object Application extends IOApp.Simple {
  implicit val logger = Slf4jLogger.getLogger[IO]
  override def run: IO[Unit] = for {
    config <- ApplicationConfig.load[IO]
    _      <- logger.info(Console.GREEN + config + Console.RESET)
    _ <- ApplicationResources.make[IO](config).use { resources =>
      for {
        health       <- Health.make[IO]
        ws           <- Ws.make[IO](resources)
        articles     <- Articles.make[IO](resources)
        translations <- Translations.make[IO](config.deepl, resources)
        http         <- Http.make[IO](health, ws, articles, translations)
        server = HttpServer[IO].ember(config.httpServer, http).use(_ => IO.never)
        _ <- Stream
          .eval(server)
          .concurrently(articles.stream)
          .concurrently(translations.stream)
          .compile
          .drain
      } yield ()
    }
  } yield ()
}
