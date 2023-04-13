package newstracker.article.db

import mongo4cats.bson.ObjectId

import newstracker.article._
import newstracker.article.domain._

import java.time.LocalDate

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
      title = ArticleTitle(title),
      content = ArticleContent(content),
      createdAt = ArticleCreatedAt(createdAt),
      language = ArticleLanguage(language),
      tags = ArticleTags(tags.getOrElse(Set.empty))
    )
}

object ArticleEntity {

  def from(article: Article): ArticleEntity =
    ArticleEntity(
      _id = ObjectId(article.id.value),
      title = article.title.value,
      content = article.content.value,
      createdAt = article.createdAt.value,
      language = article.language.value,
      tags = Some(article.tags.value)
    )

  def create(article: CreateArticle): ArticleEntity =
    ArticleEntity(
      _id = ObjectId(),
      title = article.title.value,
      content = article.content.value,
      createdAt = article.createdAt.value,
      language = article.language.value,
      tags = Some(article.tags.value)
    )
}
