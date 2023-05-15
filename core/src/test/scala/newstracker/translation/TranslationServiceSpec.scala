package newstracker.translation

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import fs2.kafka.KafkaConsumer
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.typelevel.log4cats.slf4j.Slf4jLogger

import newstracker.kafka.Producer
import newstracker.kafka.command.TranslateCommand
import newstracker.kafka.event._
import newstracker.translation.db.TranslationRepository
import newstracker.translation.deepl.DeeplClient
import newstracker.translation.{TranslationFixtures, TranslationService}

class TranslationServiceSpec extends AsyncWordSpec with Matchers with MockitoSugar {
  implicit val logger = Slf4jLogger.getLogger[IO]

  "A TranslationService" should {
    "create new translation" in {
      val translateCommandProducer = mock[Producer[IO, Unit, TranslateCommand]]
      when(translateCommandProducer.produceOne(any[Unit], any[TranslateCommand])).thenReturn(IO.unit)

      val createLocalization = TranslationFixtures.createLocalization()

      TranslationService
        .make[IO](
          mock[TranslationRepository[IO]],
          mock[DeeplClient[IO]],
          mock[KafkaConsumer[IO, Unit, CreatedArticleEvent]],
          mock[Producer[IO, Unit, TranslatedEvent]],
          translateCommandProducer,
          mock[KafkaConsumer[IO, Unit, TranslateCommand]]
        )
        .flatMap(svc => svc.createLocalization(createLocalization))
        .unsafeToFuture()
        .map { _ =>
          verify(translateCommandProducer)
            .produceOne((), TranslateCommand(createLocalization.id.value, createLocalization.language.value))

          succeed
        }
    }

    "get translation" in {
      val repo = mock[TranslationRepository[IO]]
      when(repo.get(TranslationFixtures.tid)).thenReturn(IO.pure(TranslationFixtures.translation()))

      TranslationService
        .make[IO](
          repo,
          mock[DeeplClient[IO]],
          mock[KafkaConsumer[IO, Unit, CreatedArticleEvent]],
          mock[Producer[IO, Unit, TranslatedEvent]],
          mock[Producer[IO, Unit, TranslateCommand]],
          mock[KafkaConsumer[IO, Unit, TranslateCommand]]
        )
        .flatMap(svc => svc.get(TranslationFixtures.tid))
        .unsafeToFuture()
        .map { t =>
          verify(repo).get(TranslationFixtures.tid)
          t mustBe TranslationFixtures.translation()
        }
    }

    "handle TranslateCommand" in {
      val expectedTranslation = TranslationFixtures.translation(
        localizations = List(TranslationFixtures.enLoc, TranslationFixtures.deLoc)
      )

      val repo = mock[TranslationRepository[IO]]
      when(repo.get(TranslationFixtures.tid)).thenReturn(IO.pure(TranslationFixtures.translation()))
      when(repo.update(expectedTranslation)).thenReturn(IO.pure(expectedTranslation))

      val deeplClient = mock[DeeplClient[IO]]
      when(
        deeplClient.translate(
          TranslationFixtures.enLoc.content.value,
          TranslationFixtures.enLoc.language.value,
          TranslationFixtures.deLoc.language.value
        )
      ).thenReturn(IO.pure(TranslationFixtures.deLoc.content.value))

      val createdArticleEventConsumer = mock[KafkaConsumer[IO, Unit, CreatedArticleEvent]]
      when(createdArticleEventConsumer.stream).thenReturn(fs2.Stream.empty)

      val translatedEventProducer = mock[Producer[IO, Unit, TranslatedEvent]]
      when(translatedEventProducer.produceOne(any[Unit], eqTo(TranslationFixtures.translatedEvent()))).thenReturn(IO.unit)

      val translateCommandConsumer = mock[KafkaConsumer[IO, Unit, TranslateCommand]]
      when(translateCommandConsumer.stream).thenReturn(TranslationFixtures.translateCommandStream())

      TranslationService
        .make[IO](
          repo,
          deeplClient,
          createdArticleEventConsumer,
          translatedEventProducer,
          mock[Producer[IO, Unit, TranslateCommand]],
          translateCommandConsumer
        )
        .flatMap(svc => svc.stream.take(1).compile.drain)
        .unsafeToFuture()
        .map { _ =>
          verify(repo).get(TranslationFixtures.tid)
          verify(repo).update(expectedTranslation)
          verify(translatedEventProducer).produceOne((), TranslationFixtures.translatedEvent())
          verify(deeplClient).translate(
            TranslationFixtures.enLoc.content.value,
            TranslationFixtures.enLoc.language.value,
            TranslationFixtures.deLoc.language.value
          )

          succeed
        }
    }
  }
}
