package newstracker.clients.yahoo

import cats.data.NonEmptyList
import cats.effect._
import cats.implicits._
import fs2.Stream
import org.typelevel.log4cats.Logger

import newstracker.clients.ApplicationResources
import newstracker.clients.common.SearchPipeline
import newstracker.clients.yahoo.db.ArticleRepository
import newstracker.clients.yahoo.domain._
import newstracker.kafka.Producer
import newstracker.kafka.command.CreateArticleCommand
import newstracker.kafka.event.ServiceEvent

import java.time.LocalDate

final private[yahoo] class LiveYahooPipeline[F[_]: Async](
    config: YahooConfig,
    client: YahooClient[F],
    service: ArticleService[F],
    createArticleProducer: Producer[F, Unit, CreateArticleCommand],
    serviceEventsProducer: Producer[F, Unit, ServiceEvent]
) extends SearchPipeline[F] {
  import YahooSearchPipeline._

  val serviceName = s"yahoo-${config.nameSuffix}"

  override def search(): F[Unit] = {
    def searchStream: Stream[F, Unit] = Stream
      .awakeEvery(config.pollInterval)
      .evalMap { _ =>
        for {
          newArticleIds <- client.getNewArticleIds()
          storedUuids   <- service.getAll.take(100).compile.to(Set)
        } yield NonEmptyList.fromList(newArticleIds.filterNot(storedUuids.contains))
      }
      .unNone
      .evalTap(uuids => service.create(uuids.map(uuid => CreateArticle(uuid, ArticleCreatedAt(LocalDate.now())))))
      .flatMap { uuids =>
        Stream.eval(
          serviceEventsProducer.produceOne(
            ServiceEvent.makeTaskCompletedEvent(serviceName, s"Found ${uuids.size} new articles")
          )
        ) >> Stream.emits(uuids.toList)
      }
      .evalMap(client.getArticleDetails(_).map(details => ((), details.toCreateArticleCommand(config.region))))
      .through(createArticleProducer.pipe)
      .handleErrorWith { error =>
        Stream.eval(
          serviceEventsProducer.produceOne(
            ServiceEvent.makeErrorEvent(
              s"yahoo-${config.nameSuffix}",
              s"Error while searching for new articles: ${error.getMessage}"
            )
          )
        ) >> searchStream
      }

    searchStream
      .concurrently(Stream.eval(serviceEventsProducer.produceOne(ServiceEvent.makeOnlineEvent(serviceName))))
      .onFinalize(serviceEventsProducer.produceOne(ServiceEvent.makeOfflineEvent(serviceName)))
      .compile
      .drain
  }
}

object YahooSearchPipeline {
  def make[F[_]: Async: Logger](
      config: YahooConfig,
      resources: ApplicationResources[F]
  ): F[SearchPipeline[F]] =
    for {
      client     <- YahooClient.make[F](config, resources)
      repository <- ArticleRepository.make[F](resources.mongo)
      service    <- ArticleService.make[F](repository)
    } yield new LiveYahooPipeline[F](config, client, service, resources.createArticleProducer, resources.serviceEventsProducer)

  implicit class ArticleDetailsOps(val details: ArticleDetails) extends AnyVal {
    def toCreateArticleCommand(region: String) = CreateArticleCommand(
      details.title.value,
      details.content.value,
      details.createdAt.value,
      region,
      details.authors.value,
      details.summary.value.some,
      details.url.value.some,
      details.source.value.some
    )
  }
}
