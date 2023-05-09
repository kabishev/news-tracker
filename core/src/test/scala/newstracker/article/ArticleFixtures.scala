package newstracker.article

import mongo4cats.bson.ObjectId

import newstracker.article.domain._
import newstracker.kafka.createdArticle

import java.time.LocalDate

package object ArticleFixtures {
  lazy val aid: ArticleId      = ArticleId(ObjectId().toHexString)
  lazy val aid2: ArticleId     = ArticleId(ObjectId().toHexString)
  lazy val title: ArticleTitle = ArticleTitle("title-1")
  lazy val createdAt           = ArticleCreatedAt(LocalDate.parse("2021-01-01"))

  def article(): Article = Article(
    aid,
    title,
    ArticleContent("content"),
    createdAt,
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
      language: ArticleLanguage = ArticleLanguage("en"),
      authors: ArticleAuthors = ArticleAuthors("Ivan Ivanov"),
      summary: Option[ArticleSummary] = None,
      url: Option[ArticleUrl] = None,
      source: Option[ArticleSource] = None,
      tags: Option[ArticleTags] = None
  ): CreateArticle = CreateArticle(title, content, createdAt, language, authors, summary, url, source, tags)

  def createdArticleEvent(
      id: String = aid.value,
      title: String = "title-1",
      content: String = "content",
      createdAt: LocalDate = LocalDate.parse("2021-01-01"),
      language: String = "en",
      authors: String = "Ivan Ivanov",
      summary: Option[String] = None,
      url: Option[String] = None,
      source: Option[String] = None
  ): createdArticle.Event = createdArticle.Event(id, title, content, createdAt, language, authors, summary, url, source)
}
