package newstracker.article

import mongo4cats.bson.ObjectId

import newstracker.article.domain._

import java.time.LocalDate

package object ArticleFixtures {
  lazy val aid: ArticleId      = ArticleId(ObjectId().toHexString)
  lazy val aid2: ArticleId     = ArticleId(ObjectId().toHexString)
  lazy val title: ArticleTitle = ArticleTitle("title-1")
  lazy val createdAt           = ArticleCreatedAt(LocalDate.now())

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
}
