package newstracker.clients.yahoo

import io.estatico.newtype.macros.newtype

import java.time.LocalDate

package object domain {
  @newtype case class ArticleId(value: String)
  @newtype case class ArticleUuid(value: String)
  @newtype case class ArticleAuthors(value: String)
  @newtype case class ArticleTitle(value: String)
  @newtype case class ArticleCreatedAt(value: LocalDate)
  @newtype case class ArticleContent(value: String)
  @newtype case class ArticleSummary(value: String)
  @newtype case class ArticleUrl(value: String)
  @newtype case class ArticleSource(value: String)

  final case class Article(
      id: ArticleId,
      uuid: ArticleUuid,
      createdAt: ArticleCreatedAt
  )

  final case class CreateArticle(
      uuid: ArticleUuid,
      createdAt: ArticleCreatedAt
  )

  final case class ArticleDetails(
      uuid: ArticleUuid,
      authors: ArticleAuthors,
      title: ArticleTitle,
      createdAt: ArticleCreatedAt,
      content: ArticleContent,
      summary: ArticleSummary,
      url: ArticleUrl,
      source: ArticleSource
  )
}
