package newstracker.kafka.event

import cats.effect.kernel._
import fs2.kafka._
import io.circe.generic.auto._

import newstracker.kafka._

final case class TranslatedEvent(
    id: String,
    language: String
)

object TranslatedEvent {
  private val topic = "translated-event"

  def makeConsumer[F[_]: Async](config: KafkaConfig): Resource[F, KafkaConsumer[F, Unit, TranslatedEvent]] =
    Consumer
      .make[F, Unit, TranslatedEvent](config)
      .evalTap(_.subscribeTo(topic))

  def makeProducer[F[_]: Async](config: KafkaConfig): Resource[F, Producer[F, Unit, TranslatedEvent]] =
    Producer.make[F, Unit, TranslatedEvent](config, topic)
}
