package newstracker.kafka

import cats.effect.kernel._
import fs2.kafka._
import io.circe.generic.auto._

import java.time.LocalDate

object createArticle {
  final case class Event(
      title: String,
      content: String,
      createdAt: LocalDate,
      language: String
  )

  def makeConsumer[F[_]: Async](config: KafkaConfig): Resource[F, KafkaConsumer[F, Unit, Event]] =
    Consumer
      .makeWithoutKey[F, Event](config)
      .evalTap(_.subscribeTo("create-article"))
}
