package newstracker.clients.yahoo

import io.estatico.newtype.macros.newtype

import java.time.Instant

package object domain {
  @newtype case class ArticleId(value: String)
  @newtype case class ArticleUuid(value: String)
  @newtype case class ArticleAuthors(value: String)
  @newtype case class ArticleTitle(value: String)
  @newtype case class ArticleCreatedAt(value: Instant)
  @newtype case class ArticleAddedAt(value: Instant)
  @newtype case class ArticleContent(value: String)
  @newtype case class ArticleSummary(value: String)
  @newtype case class ArticleUrl(value: String)
  @newtype case class ArticleSource(value: String)
}
