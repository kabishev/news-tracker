package newstracker.common

import cats.MonadError
import cats.implicits._
import com.mongodb.client.result.UpdateResult
import mongo4cats.bson.ObjectId
import mongo4cats.operations.Filter
import mongo4cats.operations.Update

import java.time.Instant

trait Repository[F[_]] {
  
  def isValidId(id: String): Boolean = ObjectId.isValid(id)

  protected object Field {
    val Id       = "_id"
    val Title    = "title"
    val Content  = "content"
    val CreateAt = "createdAt"
    val Language = "language"
  }

  private def idEqFilter(name: String, id: String): Filter = Filter.eq(name, ObjectId(id))
  protected def idEq(id: String): Filter                   = idEqFilter(Field.Id, id)

  protected def errorIfNull[A](error: Throwable)(res: A)(implicit F: MonadError[F, Throwable]): F[A] =
    Option(res).map(_.pure[F]).getOrElse(error.raiseError[F, A])

  protected def errorIfNoMatches(error: Throwable)(res: UpdateResult)(implicit F: MonadError[F, Throwable]): F[Unit] =
    if (res.getMatchedCount > 0) F.unit else error.raiseError[F, Unit]
}
