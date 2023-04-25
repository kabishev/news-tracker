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
      .make[F, Unit, Event](config, true)
      .evalTap(_.subscribeTo("create-article"))
}
