package newstracker.article

import cats.effect.Concurrent
import cats.effect.kernel.Async
import cats.syntax.all._
import fs2.Stream

import newstracker.article.db.ArticleRepository
import newstracker.article.domain._
import newstracker.common.Service
import newstracker.kafka.Producer
import newstracker.kafka.event._

trait ArticleService[F[_]] extends Service {
  def getAll: Stream[F, Article]
  def get(id: ArticleId): F[Article]
  def create(article: CreateArticle): F[ArticleId]
  def update(article: Article): F[Unit]
}

final private class LiveArticleService[F[_]: Concurrent](
    private val repository: ArticleRepository[F],
    private val createdArticleProducer: Producer[F, Unit, CreatedArticleEvent]
) extends ArticleService[F] {
  override def create(article: CreateArticle): F[ArticleId] = for {
    id <- repository.create(article)
    _  <- createdArticleProducer.produceOne(toCreatedArticleEvent(id, article))
  } yield id

  override def getAll: Stream[F, Article]        = repository.getAll
  override def get(id: ArticleId): F[Article]    = repository.get(id)
  override def update(article: Article): F[Unit] = repository.update(article)
  override def isValidId(id: String): Boolean    = repository.isValidId(id)

  private def toCreatedArticleEvent(id: ArticleId, createArticle: CreateArticle): CreatedArticleEvent =
    CreatedArticleEvent(
      id = id.value,
      title = createArticle.title.value,
      content = createArticle.content.value,
      createdAt = createArticle.createdAt.value,
      language = createArticle.language.value,
      authors = createArticle.authors.value,
      summary = createArticle.summary.map(_.value),
      url = createArticle.url.map(_.value),
      source = createArticle.source.map(_.value)
    )
}

object ArticleService {
  def make[F[_]: Async](
      repository: ArticleRepository[F],
      createdArticleProducer: Producer[F, Unit, CreatedArticleEvent]
  ): F[ArticleService[F]] =
    Async[F].pure(new LiveArticleService[F](repository, createdArticleProducer))
}
