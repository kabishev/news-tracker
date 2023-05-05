package newstracker.clients.common

trait SearchPipeline[F[_]] {
  def search(): F[Unit]
}
