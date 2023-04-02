package newstracker.article

import cats.effect.Async
import cats.syntax.flatMap._
import cats.syntax.functor._
import newstracker.ApplicationResources
import newstracker.article.db.ArticleRepository
import newstracker.common.Controller
import org.typelevel.log4cats.Logger

final class Articles[F[_]] private (val controller: Controller[F])

object Articles {
  def make[F[_]: Async: Logger](resources: ApplicationResources[F]): F[Articles[F]] =
    for {
      repo <- ArticleRepository.make[F](resources.mongo)
      svc  <- ArticleService.make[F](repo)
      ctrl <- ArticleController.make[F](svc)
    } yield new Articles[F](ctrl)
}
