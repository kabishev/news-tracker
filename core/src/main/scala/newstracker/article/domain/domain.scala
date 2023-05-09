package newstracker.article

import io.estatico.newtype.macros.newtype

import java.time.LocalDate

package object domain {
  @newtype case class ArticleId(value: String)
  @newtype case class ArticleTitle(value: String)
  @newtype case class ArticleContent(value: String)
  @newtype case class ArticleCreatedAt(value: LocalDate)
  @newtype case class ArticleLanguage(value: String)
  @newtype case class ArticleAuthors(value: String)
  @newtype case class ArticleSummary(value: String)
  @newtype case class ArticleUrl(value: String)
  @newtype case class ArticleSource(value: String)
  @newtype case class ArticleTags(value: Set[String])
}
