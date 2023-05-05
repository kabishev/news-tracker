package newstracker.kafka

import cats.effect._
import cats.effect.unsafe.implicits.global
import fs2._
import io.circe.generic.auto._
import net.manub.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig}
import org.apache.kafka.common.serialization.StringDeserializer
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ProducerSpec extends AnyWordSpec with Matchers with EmbeddedKafka {
  val topic                           = "topictest"
  implicit val sd: StringDeserializer = new StringDeserializer
  val baseConfig =
    EmbeddedKafkaConfig(kafkaPort = 0, zooKeeperPort = 0, customBrokerProperties = Map("zookeeper.connection.timeout.ms" -> "60000"))

  "Producer" should {
    "produce messages to a topic" in {
      withRunningKafkaOnFoundPort(baseConfig) { implicit kafkaConfig =>
        case class Event(id: String, name: String, optional: Option[String] = None)
        val config = KafkaConfig(s"localhost:${kafkaConfig.kafkaPort}", "default")
        val events = List(Event("e1", "event 1"), Event("e2", "event 2", Some("some")), Event("e3", "event 3"))

        Producer
          .make[IO, Unit, Event](config, topic)
          .use { producer =>
            Stream.emits(events.map(((), _))).covary[IO].through(producer.pipe).compile.drain
          }
          .unsafeRunSync()

        val publishedMessages = consumeNumberMessagesFrom(topic, 3)

        publishedMessages must be(
          List(
            """{"id":"e1","name":"event 1","optional":null}""",
            """{"id":"e2","name":"event 2","optional":"some"}""",
            """{"id":"e3","name":"event 3","optional":null}"""
          )
        )
      }
    }
  }
}
