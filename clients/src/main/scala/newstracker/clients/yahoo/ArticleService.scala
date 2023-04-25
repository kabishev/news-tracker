package newstracker.clients.yahoo

import cats.Monad
import cats.data.NonEmptyList

import newstracker.clients.yahoo.db.ArticleRepository
import newstracker.clients.yahoo.domain._

trait ArticleService[F[_]] {
  def create(articles: NonEmptyList[CreateArticle]): F[Unit]
  def getLast(count: Int): F[LazyList[ArticleUuid]]
}

final private class LiveArticleService[F[_]: Monad](private val repository: ArticleRepository[F]) extends ArticleService[F] {
  override def create(articles: NonEmptyList[CreateArticle]): F[Unit] = repository.create(articles)
  override def getLast(count: Int): F[LazyList[ArticleUuid]] =
    Monad[F].map(repository.getAll)(_.take(count).map(_.uuid).to(LazyList))
}

object ArticleService {
  def make[F[_]: Monad](repository: ArticleRepository[F]): F[ArticleService[F]] =
    Monad[F].pure(new LiveArticleService[F](repository))
}
