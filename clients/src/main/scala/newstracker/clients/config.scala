package newstracker.clients

import cats.effect.kernel.Sync
import pureconfig._
import pureconfig.generic.auto._

import newstracker.clients.yahoo.YahooConfig
import newstracker.kafka.KafkaConfig

import scala.concurrent.duration.FiniteDuration

object config {
  final case class ApplicationConfig(
      client: ClientConfig,
      mongo: MongoConfig,
      kafka: KafkaConfig,
      yahoo: YahooConfig
  )

  final case class ClientConfig(
      connectTimeout: FiniteDuration
  )

  final case class MongoConfig(
      connectionUri: String,
      name: String
  )

  object ApplicationConfig {
    def load[F[_]: Sync]: F[ApplicationConfig] =
      Sync[F].blocking(ConfigSource.default.loadOrThrow[ApplicationConfig])
  }
}
