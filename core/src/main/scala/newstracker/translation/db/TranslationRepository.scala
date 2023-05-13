package newstracker.translation.db

import cats.effect.Async
import cats.implicits._
import io.circe.generic.auto._
import mongo4cats.circe._
import mongo4cats.collection.MongoCollection
import mongo4cats.database.MongoDatabase
import org.typelevel.log4cats.Logger

import newstracker.common.Repository
import newstracker.translation._
import newstracker.translation.db.TranslationEntity
import newstracker.translation.domain._

trait TranslationRepository[F[_]] extends Repository[F] {
  def create(translation: Translation): F[TranslationId]
  def get(id: TranslationId): F[Translation]
  def update(translation: Translation): F[Translation]
}

final private class LiveTranslationRepository[F[_]: Async: Logger](private val collection: MongoCollection[F, TranslationEntity])
    extends TranslationRepository[F] {

  override def create(translation: Translation): F[TranslationId] =
    collection
      .insertOne(TranslationEntity.from(translation))
      .flatTap(_ => Logger[F].info(s"translation created: id = ${translation.id.value}"))
      .as(translation.id)

  override def get(id: TranslationId): F[Translation] =
    collection
      .find(idEq(id.value))
      .first
      .flatMap {
        case Some(entity) => entity.toDomain.pure[F]
        case None         => errors.TranslationDoesNotExist(id).raiseError[F, Translation]
      }
  override def update(translation: Translation): F[Translation] =
    collection
      .findOneAndReplace(idEq(translation.id.value), TranslationEntity.from(translation))
      .flatMap {
        case Some(entity) =>
          entity.toDomain
            .pure[F]
            .flatTap(_ => Logger[F].info(s"translation updated: id = ${translation.id.value}"))
        case None => errors.TranslationDoesNotExist(translation.id).raiseError[F, Translation]
      }
}

object TranslationRepository {
  def make[F[_]: Async: Logger](db: MongoDatabase[F]): F[TranslationRepository[F]] =
    db.getCollectionWithCodec[TranslationEntity]("translations")
      .map(new LiveTranslationRepository[F](_))
}
