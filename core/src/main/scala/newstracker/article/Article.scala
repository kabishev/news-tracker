package newstracker.article

import io.circe.{Decoder, Encoder}
import io.estatico.newtype.macros.newtype

import java.time.LocalDate

final case class ArticleId(value: String) extends AnyVal

final case class Article(
    id: ArticleId,
    title: String,
    content: String,
    createdAt: LocalDate,
    language: String,
    tags: Set[String]
)

final case class CreateArticle(
    title: String,
    content: String,
    createdAt: LocalDate,
    language: String,
    tags: Set[String]
)
