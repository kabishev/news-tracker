package newstracker

import cats.effect.kernel.Sync
import pureconfig._
import pureconfig.generic.auto._

import newstracker.kafka.KafkaConfig

object config {
  final case class ApplicationConfig(
      mongo: MongoConfig,
      httpServer: HttpServerConfig,
      kafka: KafkaConfig
  )

  final case class MongoConfig(
      connectionUri: String,
      name: String
  )

  final case class HttpServerConfig(
      host: String,
      port: Int
  )

  final case class Topic(article: String)

  object ApplicationConfig {
    def load[F[_]: Sync]: F[ApplicationConfig] =
      Sync[F].blocking(ConfigSource.default.loadOrThrow[ApplicationConfig])
  }
}
