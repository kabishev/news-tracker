package newstracker

import mongo4cats.bson.syntax._
import mongo4cats.bson.{Document, ObjectId}

import newstracker.article.domain.ArticleId
import newstracker.translation.domain.{Localization, TranslationId}

import java.time.Instant

trait MongoOps {
  def articleDocument(
      id: ArticleId,
      title: String,
      content: String,
      createdAt: Instant = Instant.now(),
      addedAt: Instant = Instant.now(),
      language: String = "en",
      authors: String = "Ivan Ivanov",
      tags: List[String] = List.empty
  ): Document = Document(
    "_id"       -> ObjectId(id.value).toBson,
    "title"     -> title.toBson,
    "content"   -> content.toBson,
    "createdAt" -> createdAt.toBson,
    "addedAt"   -> addedAt.toBson,
    "language"  -> language.toBson,
    "authors"   -> authors.toBson,
    "tags"      -> tags.toBson
  )

  def translationDocument(
      id: TranslationId,
      localizations: List[Localization]
  ): Document = Document(
    "_id" -> ObjectId(id.value).toBson,
    "localizations" -> localizations
      .map(l =>
        Document(
          "language" -> l.language.value.toBson,
          "content"  -> l.content.value.toBson
        )
      )
      .toBson
  )
}
