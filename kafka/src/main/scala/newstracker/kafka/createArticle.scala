package newstracker.kafka

import cats.effect.kernel._
import fs2.kafka._
import io.circe.generic.auto._

import java.time.LocalDate

object createArticle {
  private val topic = "create-article"

  final case class Event(
      title: String,
      content: String,
      createdAt: LocalDate,
      language: String,
      authors: String,
      summary: Option[String],
      url: Option[String],
      source: Option[String]
  )

  def makeConsumer[F[_]: Async](config: KafkaConfig): Resource[F, KafkaConsumer[F, Unit, Event]] =
    Consumer
      .makeWithoutKey[F, Event](config)
      .evalTap(_.subscribeTo(topic))

  def makeProducer[F[_]: Async](config: KafkaConfig): Resource[F, Producer[F, Unit, Event]] =
    Producer.make[F, Unit, Event](config, topic)
}
