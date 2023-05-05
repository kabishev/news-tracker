package newstracker

import cats.effect._
import fs2.kafka.KafkaConsumer
import mongo4cats.client.MongoClient
import mongo4cats.database.MongoDatabase

import newstracker.config._
import newstracker.kafka.KafkaConfig

sealed trait ApplicationResources[F[_]] {
  val mongo: MongoDatabase[F]
  val createArticleConsumer: KafkaConsumer[F, Unit, kafka.createArticle.Event]
}

object ApplicationResources {
  def make[F[_]: Async](config: ApplicationConfig): Resource[F, ApplicationResources[F]] = {
    def mongoDb(config: MongoConfig): Resource[F, MongoDatabase[F]] =
      MongoClient
        .fromConnectionString[F](config.connectionUri)
        .evalMap(_.getDatabase(config.name))

    def createArticle(config: KafkaConfig): Resource[F, KafkaConsumer[F, Unit, kafka.createArticle.Event]] =
      kafka.createArticle.makeConsumer[F](config)

    for {
      db            <- mongoDb(config.mongo)
      createArticle <- createArticle(config.kafka)
    } yield new ApplicationResources[F] {
      override val mongo: MongoDatabase[F]                                                  = db
      override val createArticleConsumer: KafkaConsumer[F, Unit, kafka.createArticle.Event] = createArticle
    }
  }
}
