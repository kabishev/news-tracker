package newstracker.article

import cats.effect.Async
import cats.syntax.flatMap._
import cats.syntax.functor._

import newstracker.ApplicationResources
import newstracker.article.db.ArticleRepository
import newstracker.common.Controller

final class Articles[F[_]] private (val controller: Controller[F], val kafka: ArticleKafka[F])

object Articles {
  def make[F[_]: Async](resources: ApplicationResources[F]): F[Articles[F]] =
    for {
      repo  <- ArticleRepository.make[F](resources.mongo)
      svc   <- ArticleService.make[F](repo, resources.createdArticleEventProducer)
      ctrl  <- ArticleController.make[F](svc)
      kafka <- ArticleKafka.make[F](svc, resources.createArticleCommandConsumer)
    } yield new Articles[F](ctrl, kafka)
}
