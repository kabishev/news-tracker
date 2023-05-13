package newstracker.translation

import cats.effect.IO
import fs2.kafka._
import mongo4cats.bson.ObjectId
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition

import newstracker.kafka.command.TranslateCommand
import newstracker.kafka.event.TranslatedEvent
import newstracker.translation.domain._

package object TranslationFixtures {
  lazy val tid: TranslationId  = TranslationId(ObjectId().toHexString)
  lazy val tid2: TranslationId = TranslationId(ObjectId().toHexString)
  lazy val enLoc: Localization = Localization(
    language = LocalizationLanguage("en"),
    content = LocalizationContent("english content")
  )
  lazy val deLoc: Localization = Localization(
    language = LocalizationLanguage("de"),
    content = LocalizationContent("deutsch Inhalt")
  )

  def translation(
      id: TranslationId = tid,
      localizations: List[Localization] = List(enLoc)
  ): Translation = Translation(id, localizations)

  def createLocalization(): CreateLocalization = CreateLocalization(
    id = tid,
    language = enLoc.language
  )

  def translateCommand(
      id: String = tid.value,
      language: String = deLoc.language.value
  ): TranslateCommand = TranslateCommand(id, language)

  def translateCommandStream() = fs2.Stream.eval(
    IO.pure(
      CommittableConsumerRecord(
        record = ConsumerRecord(
          topic = "translate-command",
          partition = 0,
          offset = 0,
          key = (),
          value = translateCommand()
        ),
        offset = CommittableOffset[IO](
          topicPartition = new TopicPartition("translate-command", 0),
          offsetAndMetadata = new OffsetAndMetadata(0L),
          consumerGroupId = Some("default"),
          commit = _ => IO.unit
        )
      )
    )
  )

  def translatedEvent(
      id: String = tid.value,
      language: String = deLoc.language.value
  ): TranslatedEvent = TranslatedEvent(id, language)
}
