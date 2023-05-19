package newstracker.article.db

import cats.effect.Async
import cats.implicits._
import fs2.Stream
import io.circe.generic.auto._
import mongo4cats.circe._
import mongo4cats.collection.MongoCollection
import mongo4cats.database.MongoDatabase

import newstracker.article.domain._
import newstracker.article.errors
import newstracker.common.Repository

trait ArticleRepository[F[_]] extends Repository[F] {
  def create(article: CreateArticle): F[ArticleId]
  def getAll: Stream[F, Article]
  def get(id: ArticleId): F[Article]
  def update(article: Article): F[Unit]
}

final private class LiveArticleRepository[F[_]: Async](private val collection: MongoCollection[F, ArticleEntity])
    extends ArticleRepository[F] {

  override def create(article: CreateArticle): F[ArticleId] = {
    val create = ArticleEntity.create(article)
    collection
      .insertOne(create)
      .as(ArticleId(create._id.toHexString))
  }

  override def getAll: Stream[F, Article] = // TODO: Needs to be refactored to better performance
    collection.find
      .stream
      .map(_.toDomain)

  override def get(id: ArticleId): F[Article] =
    collection
      .find(idEq(id.value))
      .first
      .flatMap {
        case Some(entity) => entity.toDomain.pure[F]
        case None         => errors.ArticleDoesNotExist(id).raiseError[F, Article]
      }

  override def update(article: Article): F[Unit] =
    collection
      .findOneAndReplace(idEq(article.id.value), ArticleEntity.from(article))
      .flatMap {
        case Some(_) => ().pure[F]
        case None    => errors.ArticleDoesNotExist(article.id).raiseError[F, Unit]
      }
}

object ArticleRepository {
  def make[F[_]: Async](db: MongoDatabase[F]): F[ArticleRepository[F]] =
    db.getCollectionWithCodec[ArticleEntity]("articles")
      .map(new LiveArticleRepository[F](_))
}
