package newstracker.kafka.event

import cats.effect.kernel._
import fs2.kafka._
import io.circe.generic.auto._

import newstracker.kafka._

import java.time.Instant

final case class CreatedArticleEvent(
    id: String,
    title: String,
    content: String,
    createdAt: Instant,
    language: String,
    authors: String,
    summary: Option[String],
    url: Option[String],
    source: Option[String]
)

object CreatedArticleEvent {
  private val topic = "created-article-event"

  def makeConsumer[F[_]: Async](config: KafkaConfig): Resource[F, KafkaConsumer[F, Unit, CreatedArticleEvent]] =
    Consumer
      .make[F, Unit, CreatedArticleEvent](config)
      .evalTap(_.subscribeTo(topic))

  def makeProducer[F[_]: Async](config: KafkaConfig): Resource[F, Producer[F, Unit, CreatedArticleEvent]] =
    Producer.make[F, Unit, CreatedArticleEvent](config, topic)
}
