package newstracker.kafka.command

import cats.effect.kernel._
import fs2.kafka._
import io.circe.generic.auto._

import newstracker.kafka._

final case class TranslateCommand(
    id: String,
    language: String
)

object TranslateCommand {
  private val topic = "translate-command"

  def makeConsumer[F[_]: Async](config: KafkaConfig): Resource[F, KafkaConsumer[F, Unit, TranslateCommand]] =
    Consumer
      .make[F, Unit, TranslateCommand](config)
      .evalTap(_.subscribeTo(topic))

  def makeProducer[F[_]: Async](config: KafkaConfig): Resource[F, Producer[F, Unit, TranslateCommand]] =
    Producer.make[F, Unit, TranslateCommand](config, topic)
}
