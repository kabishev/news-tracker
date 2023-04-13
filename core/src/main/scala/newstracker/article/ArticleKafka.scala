package newstracker.article

import cats.Monad
import cats.effect.kernel.Async
import cats.implicits._
import fs2.kafka._

import newstracker.article.domain._

import scala.concurrent.duration._
trait ArticleKafka[F[_]] {
  def stream: fs2.Stream[F, Unit]
}

final private class LiveArticleKafka[F[_]: Async](
    private val service: ArticleService[F],
    private val createArticleConsumer: KafkaConsumer[F, Unit, newstracker.kafka.createArticle.Event]
) extends ArticleKafka[F] {
  def stream: fs2.Stream[F, Unit] =
    createArticleConsumerStream

  private def createArticleConsumerStream: fs2.Stream[F, Unit] =
    createArticleConsumer
      .stream
      .mapAsync(16) { commitable =>
        service
          .create(commitable.record.value.toCreateArticle)
          .as(commitable.offset)
      }
      .through(commitBatchWithin(500, 10.seconds))

  private implicit class CreateArticleEvent(event: newstracker.kafka.createArticle.Event) {
    def toCreateArticle: CreateArticle =
      CreateArticle(
        title = event.title,
        content = event.content,
        createdAt = event.createdAt,
        language = event.language,
        tags = Set.empty
      )
  }
}

object ArticleKafka {
  def make[F[_]: Async](
      service: ArticleService[F],
      createArticleConsumer: KafkaConsumer[F, Unit, newstracker.kafka.createArticle.Event]
  ): F[ArticleKafka[F]] =
    Monad[F].pure(new LiveArticleKafka[F](service, createArticleConsumer))
}

