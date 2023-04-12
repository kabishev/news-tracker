package newstracker

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec

import config._

class ConfigSpec extends AsyncWordSpec with Matchers {

  System.setProperty("HOST", "1.2.3.4")
  System.setProperty("PORT", "1234")
  System.setProperty("MONGO_HOST", "mongo")
  System.setProperty("MONGO_USER", "user")
  System.setProperty("MONGO_PASSWORD", "password")
  System.setProperty("KAFKA_SERVERS", "kafka:1234")
  System.setProperty("KAFKA_GROUP_ID", "kafka-group")

  "An Config" should {

    "load itself from application.conf" in {
      val config = ApplicationConfig.load[IO]

      config.unsafeToFuture().map { c =>
        c.httpServer.host mustBe "1.2.3.4"
        c.httpServer.port mustBe 1234
        c.mongo.connectionUri mustBe "mongodb://user:password@mongo/news-tracker"
        c.kafka.servers mustBe "kafka:1234"
        c.kafka.groupId mustBe "kafka-group"
      }
    }
  }
}
