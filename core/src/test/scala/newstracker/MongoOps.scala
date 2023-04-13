package newstracker

import mongo4cats.bson.syntax._
import mongo4cats.bson.{Document, ObjectId}

import newstracker.article.domain.ArticleId

import java.time.{Instant, LocalDate}

trait MongoOps {
  def articleDocument(
      id: ArticleId,
      title: String,
      content: String,
      createdAt: LocalDate = LocalDate.now(),
      language: String = "en",
      tags: List[String] = List.empty
  ): Document = Document(
    "_id"       -> ObjectId(id.value).toBson,
    "title"     -> title.toBson,
    "content"   -> content.toBson,
    "createdAt" -> Instant.ofEpochSecond(createdAt.toEpochDay * 86400).toBson,
    "language"  -> language.toBson,
    "tags"      -> tags.toBson
  )
}
