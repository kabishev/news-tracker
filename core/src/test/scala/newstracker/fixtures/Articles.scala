package newstracker.fixtures

import mongo4cats.bson.ObjectId

import newstracker.article.domain._

import java.time.LocalDate

object Articles {
  lazy val aid: ArticleId      = ArticleId(ObjectId().toHexString)
  lazy val aid2: ArticleId     = ArticleId(ObjectId().toHexString)
  lazy val title: ArticleTitle = ArticleTitle("title-1")

  def article(): Article = Article(
    aid,
    title,
    ArticleContent("content"),
    ArticleCreatedAt(LocalDate.now()),
    ArticleLanguage("en"),
    ArticleTags(Set.empty)
  )

  def create(): CreateArticle = CreateArticle(
    title,
    ArticleContent("content"),
    ArticleCreatedAt(LocalDate.now()),
    ArticleLanguage("en"),
    ArticleTags(Set.empty)
  )
}
