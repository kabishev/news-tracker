package newstracker.clients

import cats.effect._
import mongo4cats.client.MongoClient
import mongo4cats.database.MongoDatabase
import sttp.client3.httpclient.fs2.HttpClientFs2Backend
import sttp.client3.{SttpBackend, SttpBackendOptions}

import newstracker.clients.config._
import newstracker.kafka._

trait ApplicationResources[F[_]] {
  def httpClientBackend: SttpBackend[F, Any]
  def mongo: MongoDatabase[F]
  def createArticleProducer: Producer[F, Unit, createArticle.Event]
}

object ApplicationResources {
  def make[F[_]: Async](config: ApplicationConfig): Resource[F, ApplicationResources[F]] = {

    def clientBackend(config: ClientConfig): Resource[F, SttpBackend[F, Any]] =
      HttpClientFs2Backend.resource[F](SttpBackendOptions(connectionTimeout = config.connectTimeout, proxy = None))

    def mongoDb(config: MongoConfig): Resource[F, MongoDatabase[F]] =
      MongoClient
        .fromConnectionString[F](config.connectionUri)
        .evalMap(_.getDatabase(config.name))

    def articleProducer(config: KafkaConfig): Resource[F, Producer[F, Unit, createArticle.Event]] =
      createArticle.makeProducer[F](config)

    for {
      db              <- mongoDb(config.mongo)
      backend         <- clientBackend(config.client)
      articleProducer <- articleProducer(config.kafka)
    } yield new ApplicationResources[F] {
      override val httpClientBackend: SttpBackend[F, Any]                        = backend
      override val mongo: MongoDatabase[F]                                       = db
      override def createArticleProducer: Producer[F, Unit, createArticle.Event] = articleProducer
    }
  }
}
