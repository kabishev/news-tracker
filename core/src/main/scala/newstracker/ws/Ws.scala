package newstracker.ws

import cats.effect.Async
import cats.syntax.functor._

import newstracker.ApplicationResources
import newstracker.common.Controller

final class Ws[F[_]] private (val controller: Controller[F])

object Ws {
  def make[F[_]: Async](resources: ApplicationResources[F]): F[Ws[F]] =
    WsController
      .make[F](
        resources.createdArticleEventConsumer,
        resources.translatedEventConsumer
      )
      .map(c => new Ws[F](c))
}
