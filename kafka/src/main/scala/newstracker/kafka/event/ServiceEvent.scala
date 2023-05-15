package newstracker.kafka.event

import cats.effect.kernel._
import fs2.kafka._
import io.circe.generic.auto._

import newstracker.kafka._

sealed trait ServiceEvent

final case class OnlineEvent(
    id: String,
    name: String
) extends ServiceEvent

final case class OfflineEvent(
    id: String
) extends ServiceEvent

final case class TaskCompletedEvent(
    id: String,
    description: String,
    duration: Long,
    result: String
) extends ServiceEvent

final case class ErrorEvent(
    id: String,
    error: String
) extends ServiceEvent


object ServiceEvent {
  private val topic = "service-events"

  def makeConsumer[F[_]: Async](config: KafkaConfig): Resource[F, KafkaConsumer[F, Unit, ServiceEvent]] =
    Consumer
      .make[F, Unit, ServiceEvent](config)
      .evalTap(_.subscribeTo(topic))

  def makeProducer[F[_]: Async](config: KafkaConfig): Resource[F, Producer[F, Unit, ServiceEvent]] =
    Producer.make[F, Unit, ServiceEvent](config, topic)
}
