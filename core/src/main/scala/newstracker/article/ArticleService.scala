package newstracker.article

import cats.Monad
import fs2.Stream

import newstracker.article.db.ArticleRepository
import newstracker.article.domain._
import newstracker.common.Service

trait ArticleService[F[_]] extends Service {
  def getAll: Stream[F, Article]
  def get(id: ArticleId): F[Article]
  def create(article: CreateArticle): F[ArticleId]
  def update(article: Article): F[Unit]
}

final private class LiveArticleService[F[_]](private val repository: ArticleRepository[F]) extends ArticleService[F] {
  override def create(tx: CreateArticle): F[ArticleId] = repository.create(tx)
  override def getAll: Stream[F, Article]              = repository.getAll
  override def get(id: ArticleId): F[Article]          = repository.get(id)
  override def update(article: Article): F[Unit]       = repository.update(article)
  override def isValidId(id: String): Boolean          = repository.isValidId(id)
}

object ArticleService {
  def make[F[_]: Monad](repository: ArticleRepository[F]): F[ArticleService[F]] =
    Monad[F].pure(new LiveArticleService[F](repository))
}
