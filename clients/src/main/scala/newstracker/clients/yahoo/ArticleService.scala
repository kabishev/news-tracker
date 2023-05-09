package newstracker.clients.yahoo

import cats.data.NonEmptyList
import cats.effect.kernel.Async
import fs2.Stream

import newstracker.clients.yahoo.db.ArticleRepository
import newstracker.clients.yahoo.domain._

trait ArticleService[F[_]] {
  def create(articles: NonEmptyList[CreateArticle]): F[Unit]
  def getAll: Stream[F, ArticleUuid]
}

final private class LiveArticleService[F[_]](private val repository: ArticleRepository[F]) extends ArticleService[F] {
  override def create(articles: NonEmptyList[CreateArticle]): F[Unit] = repository.create(articles)
  override def getAll: Stream[F, ArticleUuid] =
    repository.getAll.map(_.uuid)
}

object ArticleService {
  def make[F[_]: Async](repository: ArticleRepository[F]): F[ArticleService[F]] =
    Async[F].pure(new LiveArticleService[F](repository))
}
