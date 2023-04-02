package newstracker.article.db

import io.circe.Codec
import mongo4cats.bson.ObjectId

import java.time.{Instant, LocalDate}
import newstracker.article._

final case class ArticleEntity(
    _id: ObjectId,
    title: String,
    content: String,
    createdAt: LocalDate,
    language: String,
    tags: Option[Set[String]]
) {
  def toDomain: Article =
    Article(
      id = ArticleId(_id.toHexString),
      title = title,
      content = content,
      createdAt = createdAt,
      language = language,
      tags = tags.getOrElse(Set.empty)
    )
}

object ArticleEntity {

  def from(article: Article): ArticleEntity =
    ArticleEntity(
      _id = ObjectId(),
      title = article.title,
      content = article.content,
      createdAt = article.createdAt,
      language = article.language,
      tags = Some(article.tags)
    )

  def create(article: CreateArticle): ArticleEntity =
    ArticleEntity(
      _id = ObjectId(),
      title = article.title,
      content = article.content,
      createdAt = article.createdAt,
      language = article.language,
      tags = Some(article.tags)
    )
}
