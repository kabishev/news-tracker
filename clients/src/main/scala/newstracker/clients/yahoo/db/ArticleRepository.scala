package newstracker.clients.yahoo.db

import cats.data.NonEmptyList
import cats.effect.Async
import cats.implicits._
import io.circe.generic.auto._
import mongo4cats.circe._
import mongo4cats.collection.MongoCollection
import mongo4cats.database.MongoDatabase

import newstracker.clients.common.Repository
import newstracker.clients.yahoo.domain._

trait ArticleRepository[F[_]] extends Repository[F] {
  def create(articles: NonEmptyList[CreateArticle]): F[Unit]
  def getAll: F[LazyList[Article]]
}

final private class LiveTransactionRepository[F[_]: Async](private val collection: MongoCollection[F, ArticleEntity])
    extends ArticleRepository[F] {

  override def create(articles: NonEmptyList[CreateArticle]): F[Unit] = {
    val entities = articles.map(ArticleEntity.create)
    collection
      .insertMany(entities.toList)
      .as(())
  }

  override def getAll: F[LazyList[Article]] =
    collection.find
      .sortByDesc(Field.CreateAt)
      .all
      .map(_.map(_.toDomain).to(LazyList))
}

object ArticleRepository {
  def make[F[_]: Async](db: MongoDatabase[F]): F[ArticleRepository[F]] =
    db.getCollectionWithCodec[ArticleEntity]("yahoo-articles")
      .map(new LiveTransactionRepository[F](_))
}
