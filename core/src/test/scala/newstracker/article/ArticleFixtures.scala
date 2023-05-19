package newstracker.article

import mongo4cats.bson.ObjectId

import newstracker.article.domain._
import newstracker.kafka.event.CreatedArticleEvent

import java.time.Instant

package object ArticleFixtures {
  lazy val aid: ArticleId      = ArticleId(ObjectId().toHexString)
  lazy val aid2: ArticleId     = ArticleId(ObjectId().toHexString)
  lazy val title: ArticleTitle = ArticleTitle("title-1")
  lazy val createdAt           = ArticleCreatedAt(Instant.parse("2023-05-19T14:30:45.123Z"))
  lazy val addedAt             = ArticleAddedAt(Instant.parse("2023-04-19T14:30:45.123Z"))

  def article(): Article = Article(
    aid,
    title,
    ArticleContent("content"),
    createdAt,
    addedAt,
    ArticleLanguage("en"),
    ArticleAuthors("Ivan Ivanov"),
    None,
    None,
    None,
    None
  )

  def create(
      title: ArticleTitle = title,
      content: ArticleContent = ArticleContent("content"),
      createdAt: ArticleCreatedAt = createdAt,
      addedAt: ArticleAddedAt = addedAt,
      language: ArticleLanguage = ArticleLanguage("en"),
      authors: ArticleAuthors = ArticleAuthors("Ivan Ivanov"),
      summary: Option[ArticleSummary] = None,
      url: Option[ArticleUrl] = None,
      source: Option[ArticleSource] = None,
      tags: Option[ArticleTags] = None
  ): CreateArticle = CreateArticle(title, content, createdAt, addedAt, language, authors, summary, url, source, tags)

  def createdArticleEvent(
      id: String = aid.value,
      title: String = "title-1",
      content: String = "content",
      createdAt: Instant = Instant.parse("2023-05-19T14:30:45.123Z"),
      language: String = "en",
      authors: String = "Ivan Ivanov",
      summary: Option[String] = None,
      url: Option[String] = None,
      source: Option[String] = None
  ): CreatedArticleEvent = CreatedArticleEvent(id, title, content, createdAt, language, authors, summary, url, source)
}
