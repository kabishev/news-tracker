package newstracker.ws

import cats.effect.Async
import cats.syntax.functor._
import org.typelevel.log4cats.Logger

import newstracker.ApplicationResources
import newstracker.common.Controller

final class Ws[F[_]] private (val controller: Controller[F])

object Ws {
  def make[F[_]: Async: Logger](resources: ApplicationResources[F]): F[Ws[F]] =
    WsController
      .make[F](
        resources.createdArticleEventConsumer,
        resources.translatedEventConsumer,
        resources.serviceEventConsumer
      )
      .map(c => new Ws[F](c))
}
