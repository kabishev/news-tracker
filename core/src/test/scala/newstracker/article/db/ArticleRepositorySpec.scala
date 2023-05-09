package newstracker.article.db

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import mongo4cats.bson.ObjectId
import mongo4cats.client.MongoClient
import mongo4cats.database.MongoDatabase
import mongo4cats.embedded.EmbeddedMongo
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec

import newstracker.article.ArticleFixtures
import newstracker.article.domain.{ArticleId, _}
import newstracker.{MongoOps, article}

import scala.concurrent.Future

class ArticleRepositorySpec extends AsyncWordSpec with Matchers with EmbeddedMongo with MongoOps {
  override protected val mongoPort: Int = 12348

  private def fixMacOsXPlatform(): Unit = {
    val os: String = System.getProperty("os.name")
    if (os.startsWith("Mac OS X")) {
      val _ = System.setProperty("de.flapdoodle.os.override", "OS_X|X86_64")
    }
  }

  fixMacOsXPlatform()

  "A ArticleRepository" when {
    "create" should {
      "create a new article in database" in {
        withEmbeddedMongoDb { db =>
          val create = ArticleFixtures.create()
          val actual = for {
            repo    <- ArticleRepository.make(db)
            id      <- repo.create(create)
            article <- repo.get(id)
          } yield article

          actual.map { a =>
            a.title mustBe create.title
            a.content mustBe create.content
            a.createdAt mustBe create.createdAt
            a.language mustBe create.language
            a.tags mustBe create.tags
          }
        }
      }
    }

    "get" should {
      "return error when article id do not match" in {
        withEmbeddedMongoDb { db =>
          val actual = for {
            repo    <- ArticleRepository.make(db)
            article <- repo.get(ArticleFixtures.aid2)
          } yield article

          actual.attempt.map(_ mustBe Left(article.errors.ArticleDoesNotExist(ArticleFixtures.aid2)))
        }
      }
    }

    "getAll" should {
      "return all articles" in {
        withEmbeddedMongoDb { db =>
          val actual = for {
            repo     <- ArticleRepository.make(db)
            articles <- repo.getAll.compile.toList
          } yield articles

          actual.map { as =>
            as must have size 1
            as.head.id mustBe ArticleFixtures.aid
            as.head.title mustBe ArticleFixtures.title
          }
        }
      }
    }

    "update" should {
      "update existing category" in {
        withEmbeddedMongoDb { db =>
          val update = ArticleFixtures.article().copy(title = ArticleTitle("title-upd"))
          val actual = for {
            repo     <- ArticleRepository.make(db)
            _        <- repo.update(update)
            articles <- repo.getAll.compile.toList
          } yield articles

          actual.map { as =>
            as must have size 1
            as.head mustBe update
          }
        }
      }

      "return error when category does not exist" in {
        withEmbeddedMongoDb { db =>
          val update = ArticleFixtures.article().copy(id = ArticleId(ObjectId().toHexString), title = ArticleTitle("title-upd"))
          val actual = for {
            repo     <- ArticleRepository.make(db)
            _        <- repo.update(update)
            articles <- repo.getAll.compile.toList
          } yield articles

          actual.attempt.map(_ mustBe Left(article.errors.ArticleDoesNotExist(update.id)))
        }
      }
    }
  }

  def withEmbeddedMongoDb[A](test: MongoDatabase[IO] => IO[A]): Future[A] =
    withRunningEmbeddedMongo[IO, A] {
      MongoClient
        .fromConnectionString[IO](s"mongodb://localhost:$mongoPort")
        .use { client =>
          for {
            db       <- client.getDatabase("news-tracker")
            articles <- db.getCollection("articles")
            _        <- articles.insertMany(List(articleDocument(ArticleFixtures.aid, ArticleFixtures.title.value, "content")))
            res      <- test(db)
          } yield res
        }
    }.unsafeToFuture()
}
