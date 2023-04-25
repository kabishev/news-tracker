package newstracker

import cats.effect.kernel.Sync
import pureconfig._
import pureconfig.generic.auto._

import newstracker.kafka.KafkaConfig
import com.comcast.ip4s.Port
import com.typesafe.config.Config
import pureconfig.error.ConfigReaderFailures
import pureconfig.error.CannotConvert

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
      port: Port
  )

  implicit val portReader: ConfigReader[Port] = ConfigReader.fromString { str =>
    Port.fromString(str).toRight(CannotConvert(str, "Port", "Invalid port number"))
  }

  object ApplicationConfig {
    def load[F[_]: Sync]: F[ApplicationConfig] =
      Sync[F].blocking(ConfigSource.default.loadOrThrow[ApplicationConfig])
  }
}
