package newstracker

import cats.effect._
import fs2.kafka.KafkaConsumer
import mongo4cats.client.MongoClient
import mongo4cats.database.MongoDatabase

import newstracker.config._
import newstracker.kafka.{KafkaConfig, Producer}

sealed trait ApplicationResources[F[_]] {
  val mongo: MongoDatabase[F]
  val createArticleConsumer: KafkaConsumer[F, Unit, kafka.createArticle.Event]
  val createdArticleProducer: Producer[F, Unit, kafka.createdArticle.Event]
  val createdArticleConsumer: KafkaConsumer[F, Unit, kafka.createdArticle.Event]
}

object ApplicationResources {
  def make[F[_]: Async](config: ApplicationConfig): Resource[F, ApplicationResources[F]] = {
    def mongoDb(config: MongoConfig): Resource[F, MongoDatabase[F]] =
      MongoClient
        .fromConnectionString[F](config.connectionUri)
        .evalMap(_.getDatabase(config.name))

    for {
      db                 <- mongoDb(config.mongo)
      createArticleCons  <- kafka.createArticle.makeConsumer[F](config.kafka)
      createdArticleProd <- kafka.createdArticle.makeProducer[F](config.kafka)
      createdArticleCons <- kafka.createdArticle.makeConsumer[F](config.kafka)
    } yield new ApplicationResources[F] {
      override val mongo: MongoDatabase[F]                                                    = db
      override val createArticleConsumer: KafkaConsumer[F, Unit, kafka.createArticle.Event]   = createArticleCons
      override val createdArticleProducer: Producer[F, Unit, kafka.createdArticle.Event]      = createdArticleProd
      override val createdArticleConsumer: KafkaConsumer[F, Unit, kafka.createdArticle.Event] = createdArticleCons
    }
  }
}
