package newstracker.clients.yahoo.db

import cats.data.NonEmptyList
import cats.effect.Async
import cats.implicits._
import com.github.dwickern.macros.NameOf._
import fs2.Stream
import io.circe.generic.auto._
import mongo4cats.circe._
import mongo4cats.collection.MongoCollection
import mongo4cats.database.MongoDatabase

import newstracker.clients.yahoo.domain._

trait ArticleRepository[F[_]] {
  def create(articles: NonEmptyList[CreateArticle]): F[Unit]
  def getAll: Stream[F, Article]
}

final private class LiveTransactionRepository[F[_]: Async](private val collection: MongoCollection[F, ArticleEntity])
    extends ArticleRepository[F] {

  override def create(articles: NonEmptyList[CreateArticle]): F[Unit] = {
    val entities = articles.map(ArticleEntity.create)
    collection
      .insertMany(entities.toList)
      .as(())
  }

  override def getAll: Stream[F, Article] =
    collection.find
      .sortByDesc(nameOf[ArticleEntity](_.createdAt))
      .stream
      .map(_.toDomain)
}

object ArticleRepository {
  def make[F[_]: Async](db: MongoDatabase[F]): F[ArticleRepository[F]] =
    db.getCollectionWithCodec[ArticleEntity]("yahoo-articles")
      .map(new LiveTransactionRepository[F](_))
}
