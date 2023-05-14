package newstracker

import cats.effect._
import fs2.kafka.KafkaConsumer
import mongo4cats.client.MongoClient
import mongo4cats.database.MongoDatabase
import sttp.client3.httpclient.fs2.HttpClientFs2Backend
import sttp.client3.{SttpBackend, SttpBackendOptions}

import newstracker.config._
import newstracker.kafka._

sealed trait ApplicationResources[F[_]] {
  val mongo: MongoDatabase[F]
  def httpClientBackend: SttpBackend[F, Any]

  val createArticleCommandConsumer: KafkaConsumer[F, Unit, command.CreateArticleCommand]

  val createdArticleEventProducer: Producer[F, Unit, event.CreatedArticleEvent]
  val createdArticleEventConsumer: KafkaConsumer[F, Unit, event.CreatedArticleEvent]

  val translateCommandProducer: Producer[F, Unit, command.TranslateCommand]
  val translateCommandConsumer: KafkaConsumer[F, Unit, command.TranslateCommand]

  val translatedEventProducer: Producer[F, Unit, event.TranslatedEvent]
  val translatedEventConsumer: KafkaConsumer[F, Unit, event.TranslatedEvent]
}

object ApplicationResources {
  def make[F[_]: Async](config: ApplicationConfig): Resource[F, ApplicationResources[F]] = {

    def mongoDb(config: MongoConfig): Resource[F, MongoDatabase[F]] =
      MongoClient
        .fromConnectionString[F](config.connectionUri)
        .evalMap(_.getDatabase(config.name))

    def clientBackend(config: ClientConfig): Resource[F, SttpBackend[F, Any]] =
      HttpClientFs2Backend.resource[F](SttpBackendOptions(connectionTimeout = config.connectTimeout, proxy = None))

    for {
      db                     <- mongoDb(config.mongo)
      backend                <- clientBackend(config.client)
      createArticleConsumer  <- command.CreateArticleCommand.makeConsumer[F](config.kafka)
      createdArticleProducer <- event.CreatedArticleEvent.makeProducer[F](config.kafka)
      createdArticleConsumer <- event.CreatedArticleEvent.makeConsumer[F](config.kafka)
      translateProducer      <- command.TranslateCommand.makeProducer[F](config.kafka)
      translateConsumer      <- command.TranslateCommand.makeConsumer[F](config.kafka)
      translatedProducer     <- event.TranslatedEvent.makeProducer[F](config.kafka)
      translatedConsumer     <- event.TranslatedEvent.makeConsumer[F](config.kafka)
    } yield new ApplicationResources[F] {
      override val mongo: MongoDatabase[F]                                                            = db
      override val httpClientBackend: SttpBackend[F, Any]                                             = backend
      override val createArticleCommandConsumer: KafkaConsumer[F, Unit, command.CreateArticleCommand] = createArticleConsumer
      override val createdArticleEventProducer: Producer[F, Unit, event.CreatedArticleEvent]          = createdArticleProducer
      override val createdArticleEventConsumer: KafkaConsumer[F, Unit, event.CreatedArticleEvent]     = createdArticleConsumer
      override val translateCommandProducer: Producer[F, Unit, command.TranslateCommand]              = translateProducer
      override val translateCommandConsumer: KafkaConsumer[F, Unit, command.TranslateCommand]         = translateConsumer
      override val translatedEventProducer: Producer[F, Unit, event.TranslatedEvent]                  = translatedProducer
      override val translatedEventConsumer: KafkaConsumer[F, Unit, event.TranslatedEvent]             = translatedConsumer
    }
  }
}
