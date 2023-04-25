package newstracker.clients

import cats.effect.{IO, IOApp}
import fs2.Stream
import org.typelevel.log4cats.slf4j.Slf4jLogger

import newstracker.clients
import newstracker.clients.yahoo.YahooClient

object Application extends IOApp.Simple {
  implicit val logger = Slf4jLogger.getLogger[IO]
  override def run: IO[Unit] = for {
    config <- clients.config.ApplicationConfig.load[IO]
    _ <- ApplicationResources.make[IO](config).use { resources =>
      for {
        yahoo <- YahooClient.make[IO](config.yahoo, resources)
        _     <- Stream.eval(yahoo.search()).compile.drain
      } yield ()
    }
  } yield ()
}
