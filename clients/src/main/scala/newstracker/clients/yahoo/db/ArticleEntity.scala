package newstracker.clients.yahoo.db

import mongo4cats.bson.ObjectId

import newstracker.clients.yahoo.domain._

import java.time.Instant

final case class ArticleEntity(
    _id: ObjectId,
    uuid: String,
    createdAt: Instant
) {
  def toDomain: Article =
    Article(
      id = ArticleId(_id.toHexString),
      uuid = ArticleUuid(uuid),
      createdAt = ArticleCreatedAt(createdAt)
    )
}

object ArticleEntity {
  def from(article: Article): ArticleEntity =
    ArticleEntity(
      _id = ObjectId(article.id.value),
      uuid = article.uuid.value,
      createdAt = article.createdAt.value
    )

  def create(article: CreateArticle): ArticleEntity =
    ArticleEntity(
      _id = ObjectId(),
      uuid = article.uuid.value,
      createdAt = article.createdAt.value
    )
}
