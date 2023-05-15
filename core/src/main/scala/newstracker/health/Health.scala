package newstracker.health

import cats.effect.Async
import cats.syntax.functor._

import newstracker.common.Controller

final class Health[F[_]] private (
    val controller: Controller[F]
)

object Health {
  def make[F[_]: Async]: F[Health[F]] = HealthController.make[F].map(c => new Health[F](c))
}
