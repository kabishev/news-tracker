package newstracker.kafka.command

import cats.effect.kernel._
import fs2.kafka._
import io.circe.generic.auto._

import newstracker.kafka._

import java.time.Instant

final case class CreateArticleCommand(
    title: String,
    content: String,
    createdAt: Instant,
    addedAt: Instant,
    language: String,
    authors: String,
    summary: Option[String],
    url: Option[String],
    source: Option[String]
)

object CreateArticleCommand {
  private val topic = "create-article-command"

  def makeConsumer[F[_]: Async](config: KafkaConfig): Resource[F, KafkaConsumer[F, Unit, CreateArticleCommand]] =
    Consumer
      .make[F, Unit, CreateArticleCommand](config)
      .evalTap(_.subscribeTo(topic))

  def makeProducer[F[_]: Async](config: KafkaConfig): Resource[F, Producer[F, Unit, CreateArticleCommand]] =
    Producer.make[F, Unit, CreateArticleCommand](config, topic)
}
