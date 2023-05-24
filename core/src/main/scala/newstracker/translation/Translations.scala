package newstracker.translation

import cats.effect.Async
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.typelevel.log4cats.Logger

import newstracker.ApplicationResources
import newstracker.common.Controller
import newstracker.translation.db.TranslationRepository
import newstracker.translation.deepl.{DeeplClient, DeeplConfig}

final class Translations[F[_]] private (
  val controller: Controller[F], 
  val stream: fs2.Stream[F, Unit]
)

object Translations {
  def make[F[_]: Async: Logger](config: DeeplConfig, resources: ApplicationResources[F]): F[Translations[F]] =
    for {
      repo  <- TranslationRepository.make[F](resources.mongo)
      deepl <- DeeplClient.make[F](config, resources)
      svc <- TranslationService.make[F](
        repo,
        deepl,
        TextProcessor.makeJsoupProcessor[F],
        resources.createdArticleEventConsumer,
        resources.translatedEventProducer,
        resources.translateCommandProducer,
        resources.translateCommandConsumer,
        resources.serviceEventProducer
      )
      ctrl <- TranslationController.make[F](svc)
    } yield new Translations[F](ctrl, svc.stream)
}
