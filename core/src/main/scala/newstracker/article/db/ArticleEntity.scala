package newstracker.article.db

import mongo4cats.bson.ObjectId

import newstracker.article.domain._

import java.time.Instant

final case class ArticleEntity(
    _id: ObjectId,
    title: String,
    content: String,
    createdAt: Instant,
    addedAt: Instant,
    language: String,
    authors: String,
    summary: Option[String],
    url: Option[String],
    source: Option[String],
    tags: Option[Set[String]]
) {
  def toDomain: Article =
    Article(
      id = ArticleId(_id.toHexString),
      title = ArticleTitle(title),
      content = ArticleContent(content),
      createdAt = ArticleCreatedAt(createdAt),
      addedAt = ArticleAddedAt(addedAt),
      language = ArticleLanguage(language),
      authors = ArticleAuthors(authors),
      summary = summary.map(ArticleSummary(_)),
      url = url.map(ArticleUrl(_)),
      source = source.map(ArticleSource(_)),
      tags = tags.map(ArticleTags(_))
    )
}

object ArticleEntity {

  def from(article: Article): ArticleEntity =
    ArticleEntity(
      _id = ObjectId(article.id.value),
      title = article.title.value,
      content = article.content.value,
      createdAt = article.createdAt.value,
      addedAt = article.addedAt.value,
      language = article.language.value,
      authors = article.authors.value,
      summary = article.summary.map(_.value),
      url = article.url.map(_.value),
      source = article.source.map(_.value),
      tags = article.tags.map(_.value)
    )

  def create(article: CreateArticle): ArticleEntity =
    ArticleEntity(
      _id = ObjectId(),
      title = article.title.value,
      content = article.content.value,
      createdAt = article.createdAt.value,
      addedAt = article.addedAt.value,
      language = article.language.value,
      authors = article.authors.value,
      summary = article.summary.map(_.value),
      url = article.url.map(_.value),
      source = article.source.map(_.value),
      tags = article.tags.map(_.value)
    )
}
