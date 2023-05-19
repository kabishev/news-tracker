package newstracker.kafka.event

import cats.effect.kernel._
import fs2.kafka._
import io.circe.generic.auto._

import newstracker.kafka._

import java.util.UUID

sealed trait ServiceEvent

object ServiceEvent {
  private val topic = "service-events"

  final case class OnlineEvent(id: String, serviceName: String)                             extends ServiceEvent
  final case class OfflineEvent(id: String, serviceName: String)                            extends ServiceEvent
  final case class ErrorEvent(id: String, serviceName: String, error: String)               extends ServiceEvent
  final case class TaskCompletedEvent(id: String, serviceName: String, description: String) extends ServiceEvent

  def makeOnlineEvent(serviceName: String): ServiceEvent =
    OnlineEvent(UUID.randomUUID().toString, serviceName)

  def makeOfflineEvent(serviceName: String): ServiceEvent =
    OfflineEvent(UUID.randomUUID().toString, serviceName)

  def makeErrorEvent(serviceName: String, error: String): ServiceEvent =
    ErrorEvent(UUID.randomUUID().toString, serviceName, error)

  def makeTaskCompletedEvent(serviceName: String, description: String): ServiceEvent =
    TaskCompletedEvent(UUID.randomUUID().toString, serviceName, description)

  def makeConsumer[F[_]: Async](config: KafkaConfig): Resource[F, KafkaConsumer[F, Unit, ServiceEvent]] =
    Consumer
      .make[F, Unit, ServiceEvent](config)
      .evalTap(_.subscribeTo(topic))

  def makeProducer[F[_]: Async](config: KafkaConfig): Resource[F, Producer[F, Unit, ServiceEvent]] =
    Producer.make[F, Unit, ServiceEvent](config, topic)
}
