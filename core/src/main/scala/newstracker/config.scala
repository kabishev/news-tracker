package newstracker

import cats.effect.kernel.Sync
import com.comcast.ip4s.Port
import pureconfig._
import pureconfig.error.CannotConvert
import pureconfig.generic.auto._

import newstracker.kafka.KafkaConfig
import newstracker.translation.deepl.DeeplConfig

import scala.concurrent.duration.FiniteDuration

object config {
  final case class ApplicationConfig(
      mongo: MongoConfig,
      httpServer: HttpServerConfig,
      client: ClientConfig,
      kafka: KafkaConfig,
      deepl: DeeplConfig
  )

  final case class MongoConfig(
      connectionUri: String,
      name: String
  )

  final case class HttpServerConfig(
      host: String,
      port: Port
  )

  final case class ClientConfig(
      connectTimeout: FiniteDuration
  )

  implicit val portReader: ConfigReader[Port] = ConfigReader.fromString { str =>
    Port.fromString(str).toRight(CannotConvert(str, "Port", "Invalid port number"))
  }

  object ApplicationConfig {
    def load[F[_]: Sync]: F[ApplicationConfig] =
      Sync[F].blocking(ConfigSource.default.loadOrThrow[ApplicationConfig])
  }
}
