package newstracker.kafka

import cats.effect._
import cats.effect.unsafe.implicits.global
import io.circe.generic.auto._
import net.manub.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ConsumerSpec extends AnyWordSpec with Matchers with EmbeddedKafka {
  val topic = "topictest"
  val baseConfig =
    EmbeddedKafkaConfig(kafkaPort = 0, zooKeeperPort = 0, customBrokerProperties = Map("zookeeper.connection.timeout.ms" -> "60000"))

  "Consumer" should {
    "consume messages from a topic" in {
      withRunningKafkaOnFoundPort(baseConfig) { implicit kafkaConfig =>
        case class Event(id: String, name: String)

        val eventsToPublish = List(
          """{"id":"e1", "name": "event 1"}""",
          """{"id":"e2", "name": "event 2"}""",
          """{"id":"e3", "name": "event 3"}"""
        )

        eventsToPublish.foreach(publishStringMessageToKafka(topic, _)(kafkaConfig))

        val config = KafkaConfig(s"localhost:${kafkaConfig.kafkaPort}", "default")
        val receivedMessages = Consumer
          .make[IO, Unit, Event](config)
          .evalTap(_.subscribeTo(topic))
          .use(_.stream.evalMap(rec => IO.pure(rec.record.value)).take(3).compile.toList)
          .unsafeRunSync()

        receivedMessages must be(List(Event("e1", "event 1"), Event("e2", "event 2"), Event("e3", "event 3")))
      }
    }
  }
}
