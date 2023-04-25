package newstracker.clients.common

trait SearchArticleClient[F[_]] {
  def search(): F[Unit]
}
