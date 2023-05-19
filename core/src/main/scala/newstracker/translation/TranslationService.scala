package newstracker.translation

import cats.effect._
import cats.syntax.all._
import fs2.Stream
import fs2.kafka._
import org.typelevel.log4cats.Logger

import newstracker.common.Service
import newstracker.kafka.Producer
import newstracker.kafka.command._
import newstracker.kafka.event._
import newstracker.translation.db.TranslationRepository
import newstracker.translation.deepl.DeeplClient
import newstracker.translation.domain._

import scala.concurrent.duration._

trait TranslationService[F[_]] extends Service {
  def get(id: TranslationId): F[Translation]
  def createLocalization(createLocalization: CreateLocalization): F[Unit]
  def stream: fs2.Stream[F, Unit]
}

final private class LiveTranslationService[F[_]: Async: Logger](
    private val config: TranslationServiceConfig,
    private val repository: TranslationRepository[F],
    private val deeplClient: DeeplClient[F],
    private val createdArticleEventConsumer: KafkaConsumer[F, Unit, CreatedArticleEvent],
    private val translatedEventProducer: Producer[F, Unit, TranslatedEvent],
    private val translateCommandProducer: Producer[F, Unit, TranslateCommand],
    private val translateCommandConsumer: KafkaConsumer[F, Unit, TranslateCommand],
    private val serviceEventProducer: Producer[F, Unit, ServiceEvent]
) extends TranslationService[F] {

  override def isValidId(id: String): Boolean = repository.isValidId(id)

  override def get(id: TranslationId): F[Translation] = repository.get(id)

  override def createLocalization(createLocalization: CreateLocalization): F[Unit] =
    translateCommandProducer
      .produceOne(TranslateCommand(createLocalization.id.value, createLocalization.language.value)) >>
      Logger[F].info(s"translation command sent: id = ${createLocalization.id.value}, language = ${createLocalization.language.value}")

  override def stream: Stream[F, Unit] =
    createdArticleEventStream
      .concurrently(translateCommandStream)
      .concurrently(Stream.eval(serviceEventProducer.produceOne(ServiceEvent.makeOnlineEvent(config.name))))
      .onFinalize(serviceEventProducer.produceOne(ServiceEvent.makeOfflineEvent(config.name)))

  private def createdArticleEventStream: Stream[F, Unit] =
    createdArticleEventConsumer.stream
      .mapAsync(2) { commitable =>
        repository
          .create(commitable.record.value.toTranslation)
          .as(commitable.offset)
      }
      .through(commitBatchWithin(500, 10.seconds))
      .handleErrorWith { err =>
        Stream.eval {
          serviceEventProducer.produceOne(
            ServiceEvent.makeErrorEvent(
              config.name,
              s"translation error while processing created article event: ${err.getMessage}"
            )
          )
        } >> createdArticleEventStream
      }

  private def translateCommandStream: Stream[F, Unit] =
    translateCommandConsumer.stream
      .mapAsync(2) { commitable =>
        repository
          .get(TranslationId(commitable.record.value.id))
          .flatMap { tr =>
            translate(commitable.record.value)(tr)
          }
          .as(commitable.offset)
      }
      .through(commitBatchWithin(500, 10.seconds))
      .handleErrorWith { err =>
        Stream.eval {
          serviceEventProducer.produceOne(
            ServiceEvent.makeErrorEvent(
              config.name,
              s"translation error while processing translate command: ${err.getMessage}"
            )
          )
        } >> translateCommandStream
      }

  private def translate(cmd: TranslateCommand)(translation: Translation): F[Unit] = {
    val translationId  = TranslationId(cmd.id)
    val targetLanguage = LocalizationLanguage(cmd.language)
    val startTime      = System.currentTimeMillis()
    translation.localizations.find(_.language == targetLanguage) match {
      case None =>
        val original = translation.localizations.head
        deeplClient
          .translate(original.content.value, original.language.value, targetLanguage.value)
          .flatMap { translated =>
            Logger[F].info(s"translation received: id = ${translationId.value}, language = ${targetLanguage.value}") >>
              repository.update(
                Translation(
                  translationId,
                  translation.localizations :+ Localization(targetLanguage, LocalizationContent(translated))
                )
              )
          }
          .flatMap { updatedTranslation =>
            Logger[F].info(s"translation updated: id = ${translationId.value}, language = ${targetLanguage.value}") >>
              translatedEventProducer.produceOne(TranslatedEvent(updatedTranslation.id.value, targetLanguage.value)) >>
              serviceEventProducer.produceOne(
                ServiceEvent.makeTaskCompletedEvent(
                  config.name,
                  s"Translation (${targetLanguage.value}) completed: duration = ${System.currentTimeMillis() - startTime} ms"
                )
              )
          }
      case Some(_) => ().pure[F]
    }
  }

  implicit private class CreatedArticleEventOps(event: CreatedArticleEvent) {
    def toTranslation: Translation =
      Translation(
        id = TranslationId(event.id),
        localizations = List(
          Localization(
            language = LocalizationLanguage(event.language),
            content = LocalizationContent(event.content)
          )
        )
      )
  }
}

object TranslationService {
  def make[F[_]: Async: Logger](
      repository: TranslationRepository[F],
      deeplClient: DeeplClient[F],
      createdArticleEventConsumer: KafkaConsumer[F, Unit, CreatedArticleEvent],
      translatedEventProducer: Producer[F, Unit, TranslatedEvent],
      translateCommandProducer: Producer[F, Unit, TranslateCommand],
      translateCommandConsumer: KafkaConsumer[F, Unit, TranslateCommand],
      serviceEventProducer: Producer[F, Unit, ServiceEvent]
  ): F[TranslationService[F]] =
    Async[F].pure(
      new LiveTranslationService[F](
        TranslationServiceConfig("translation-service"),
        repository,
        deeplClient,
        createdArticleEventConsumer,
        translatedEventProducer,
        translateCommandProducer,
        translateCommandConsumer,
        serviceEventProducer
      )
    )
}
