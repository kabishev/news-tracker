package newstracker.ws

import cats.Monad
import cats.effect.Async
import cats.implicits._
import fs2.Pipe
import fs2.kafka._
import io.circe.generic.auto._
import sttp.capabilities.WebSockets
import sttp.capabilities.fs2.Fs2Streams
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.ServerEndpoint.Full
import sttp.tapir.server.http4s.Http4sServerInterpreter

import newstracker.common.{Controller, ErrorResponse}
import newstracker.kafka.event._

final private class WsController[F[_]: Async](
    private val createdArticleEventConsumer: KafkaConsumer[F, Unit, CreatedArticleEvent],
    private val translatedEventConsumer: KafkaConsumer[F, Unit, TranslatedEvent],
    private val serviceEventConsumer: KafkaConsumer[F, Unit, ServiceEvent]
) extends Controller[F] {

  override def routes =
    Http4sServerInterpreter[F](Controller.serverOptions[F]).toWebSocketRoutes(ws)

  private def ws: Full[Unit, Unit, Unit, (StatusCode, ErrorResponse), Pipe[F, Unit, WsEvent], Fs2Streams[F] with WebSockets, F] =
    Controller.publicEndpoint
      .out(webSocketBody[Unit, CodecFormat.Json, WsEvent, CodecFormat.Json](Fs2Streams[F]))
      .serverLogic { _ =>
        val createdArticleEventConsumerStream = createdArticleEventConsumer.stream
          .map(ev => WsEvent.ArticleCreated.from(ev.record.value))

        val translatedEventConsumerStream = translatedEventConsumer.stream
          .map(ev => WsEvent.ArticleTranslated.from(ev.record.value))

        val serviceEventConsumerStream = serviceEventConsumer.stream
          .map { ev =>
            ev.record.value match {
              case OnlineEvent(id, name)                                 => WsEvent.ServiceOnline(id, name)
              case OfflineEvent(id)                                      => WsEvent.ServiceOffline(id)
              case ErrorEvent(id, error)                                 => WsEvent.ServiceError(id, error)
              case TaskCompletedEvent(id, description, duration, result) => WsEvent.TaskCompleted(id, description, duration, result)
            }
          }

        def wsServerLogic: Pipe[F, Unit, WsEvent] =
          _ =>
            createdArticleEventConsumerStream
              .merge(translatedEventConsumerStream)
              .merge(serviceEventConsumerStream)

        wsServerLogic.asRight[(StatusCode, ErrorResponse)].pure[F]
      }
}

object WsController {

  def make[F[_]: Async](
      createdArticleEventConsumer: KafkaConsumer[F, Unit, CreatedArticleEvent],
      translatedEventConsumer: KafkaConsumer[F, Unit, TranslatedEvent],
      serviceEventConsumer: KafkaConsumer[F, Unit, ServiceEvent]
  ): F[Controller[F]] =
    Monad[F].pure(
      new WsController[F](
        createdArticleEventConsumer,
        translatedEventConsumer,
        serviceEventConsumer
      )
    )
}
