package newstracker.article

import io.estatico.newtype.macros.newtype

import java.time.LocalDate

package object domain {
  @newtype case class ArticleId(value: String)
  @newtype case class ArticleTitle(value: String)
  @newtype case class ArticleContent(value: String)
  @newtype case class ArticleCreatedAt(value: LocalDate)
  @newtype case class ArticleLanguage(value: String)
  @newtype case class ArticleTags(value: Set[String])

  final case class Article(
      id: ArticleId,
      title: ArticleTitle,
      content: ArticleContent,
      createdAt: ArticleCreatedAt,
      language: ArticleLanguage,
      tags: ArticleTags
  )

  final case class CreateArticle(
      title: ArticleTitle,
      content: ArticleContent,
      createdAt: ArticleCreatedAt,
      language: ArticleLanguage,
      tags: ArticleTags
  )
}
