package newstracker

import cats.effect._
import mongo4cats.client.MongoClient
import mongo4cats.database.MongoDatabase
import newstracker.config._

sealed trait ApplicationResources[F[_]] {
  val mongo: MongoDatabase[F]
}

object ApplicationResources {
  def make[F[_]: Async](config: ApplicationConfig): Resource[F, ApplicationResources[F]] = {
    def mongoDb(config: MongoConfig): Resource[F, MongoDatabase[F]] =
      MongoClient
        .fromConnectionString[F](config.connectionUri)
        .evalMap(_.getDatabase(config.name))

    mongoDb(config.mongo).map { db =>
      new ApplicationResources[F] {
        override val mongo: MongoDatabase[F] = db
      }
    }
  }
}
