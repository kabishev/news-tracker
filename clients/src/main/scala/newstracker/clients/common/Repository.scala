package newstracker.clients.common

trait Repository[F[_]] {
  protected object Field {
    val Id       = "_id"
    val Uuid     = "uuid"
    val CreateAt = "createdAt"
  }
}
