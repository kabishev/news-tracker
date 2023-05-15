package newstracker.translation.db

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import mongo4cats.bson.ObjectId
import mongo4cats.client.MongoClient
import mongo4cats.database.MongoDatabase
import mongo4cats.embedded.EmbeddedMongo
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.typelevel.log4cats.slf4j.Slf4jLogger

import newstracker.translation.TranslationFixtures
import newstracker.translation.domain.TranslationId
import newstracker.{MongoOps, translation}

import scala.concurrent.Future

class TranslationRepositorySpec extends AsyncWordSpec with Matchers with EmbeddedMongo with MongoOps {
  override protected val mongoPort: Int = 12348

  implicit val logger = Slf4jLogger.getLogger[IO]

  private def fixMacOsXPlatform(): Unit = {
    val os: String = System.getProperty("os.name")
    if (os.startsWith("Mac OS X")) {
      val _ = System.setProperty("de.flapdoodle.os.override", "OS_X|X86_64")
    }
  }

  fixMacOsXPlatform()

  "A TranslationRepository" when {
    "create" should {
      "create a new translation in database" in {
        withEmbeddedMongoDb { db =>
          val create = TranslationFixtures.translation(TranslationFixtures.tid2)
          val actual = for {
            repo        <- TranslationRepository.make(db)
            id          <- repo.create(create)
            translation <- repo.get(id)
          } yield translation

          actual.map { t =>
            t.id mustBe create.id
            t.localizations mustBe create.localizations
          }
        }
      }
    }

    "get" should {
      "return translation" in {
        withEmbeddedMongoDb { db =>
          val actual = for {
            repo        <- TranslationRepository.make(db)
            translation <- repo.get(TranslationFixtures.tid)
          } yield translation

          actual.map { t =>
            t.id mustBe TranslationFixtures.tid
            t.localizations mustBe List(TranslationFixtures.enLoc)
          }
        }
      }

      "return error when translation id do not match" in {
        withEmbeddedMongoDb { db =>
          val actual = for {
            repo    <- TranslationRepository.make(db)
            article <- repo.get(TranslationFixtures.tid2)
          } yield article

          actual.attempt.map(_ mustBe Left(translation.errors.TranslationDoesNotExist(TranslationFixtures.tid2)))
        }
      }
    }

    "update" should {
      "update existing translation" in {
        withEmbeddedMongoDb { db =>
          val update = TranslationFixtures.translation().copy(localizations = List(TranslationFixtures.deLoc))
          val actual = for {
            repo        <- TranslationRepository.make(db)
            _           <- repo.update(update)
            translation <- repo.get(update.id)
          } yield translation

          actual.map { t =>
            t.id mustBe TranslationFixtures.tid
            t.localizations mustBe List(TranslationFixtures.deLoc)
          }
        }
      }

      "return error when translation does not exist" in {
        withEmbeddedMongoDb { db =>
          val update = TranslationFixtures.translation().copy(id = TranslationId(ObjectId().toHexString))
          val actual = for {
            repo        <- TranslationRepository.make(db)
            _           <- repo.update(update)
            translation <- repo.get(update.id)
          } yield translation

          actual.attempt.map(_ mustBe Left(translation.errors.TranslationDoesNotExist(update.id)))
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
            db           <- client.getDatabase("news-tracker")
            translations <- db.getCollection("translations")
            _            <- translations.insertMany(List(translationDocument(TranslationFixtures.tid, List(TranslationFixtures.enLoc))))
            res          <- test(db)
          } yield res
        }
    }.unsafeToFuture()
}
