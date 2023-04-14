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
    ArticleTags(Set.empty)
  )

  def create(
      title: ArticleTitle = title,
      content: ArticleContent = ArticleContent("content"),
      createdAt: ArticleCreatedAt = createdAt,
      language: ArticleLanguage = ArticleLanguage("en"),
      tags: ArticleTags = ArticleTags(Set.empty)
  ): CreateArticle = CreateArticle(title, content, createdAt, language, tags)
}
