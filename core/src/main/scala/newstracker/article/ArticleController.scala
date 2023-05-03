package newstracker.article

import cats.Monad
import cats.effect.Async
import eu.timepit.refined.types.string.NonEmptyString
import io.circe.generic.auto._
import io.circe.refined._
import org.http4s.HttpRoutes
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.http4s.Http4sServerInterpreter

import newstracker.article.domain._
import newstracker.common.Controller

import java.time.LocalDate

final private class ArticleController[F[_]: Async](private val service: ArticleService[F]) extends Controller[F] {
  import ArticleController._

  def routes: HttpRoutes[F] = Http4sServerInterpreter[F](Controller.serverOptions[F]).toRoutes(
    List(
      createArticle,
      getAllArticles,
      getArticleById,
      updateArticle
    )
  )

  private val basePath = "articles"

  private val idValidator: Validator[String] = Validator.custom(
    id => if (service.isValidId(id)) ValidationResult.Valid else ValidationResult.Invalid(s"Invalid representation of an id: $id"),
    Some(s"Invalid representation of an id")
  )

  private val idPath = basePath / path[String].validate(idValidator).map((s: String) => ArticleId(s))(_.value).name("article-id")

  private def getAllArticles = Controller.publicEndpoint.get
    .in(basePath)
    .out(jsonBody[List[ArticleView]])
    .serverLogic { _ =>
      service.getAll.compile.toList.mapResponse(_.map(ArticleView.from))
    }

  private def getArticleById = Controller.publicEndpoint.get
    .in(idPath)
    .out(jsonBody[ArticleView])
    .serverLogic { id =>
      service
        .get(id)
        .mapResponse(ArticleView.from)
    }

  private def createArticle = Controller.publicEndpoint.post
    .in(basePath)
    .in(jsonBody[CreateArticleRequest])
    .out(statusCode(StatusCode.Created).and(jsonBody[CreateArticleResponse]))
    .serverLogic { req =>
      service
        .create(req.toDomain)
        .mapResponse(id => CreateArticleResponse(id.value))
    }

  private def updateArticle = Controller.publicEndpoint.put
    .in(idPath)
    .in(jsonBody[UpdateArticleRequest])
    .out(statusCode(StatusCode.NoContent))
    .serverLogic { case (id, req) =>
      service
        .update(req.toDomain(id.value))
        .voidResponse
    }
}

object ArticleController {

  def make[F[_]: Async](service: ArticleService[F]): F[Controller[F]] =
    Monad[F].pure(new ArticleController[F](service))

  final case class CreateArticleRequest(
      title: NonEmptyString,
      content: NonEmptyString,
      createdAt: LocalDate,
      language: NonEmptyString,
      authors: NonEmptyString,
      summary: Option[String],
      url: Option[String],
      source: Option[String],
      tags: Option[List[String]]
  ) {
    def toDomain: CreateArticle = CreateArticle(
      ArticleTitle(title.value),
      ArticleContent(content.value),
      ArticleCreatedAt(createdAt),
      ArticleLanguage(language.value),
      ArticleAuthors(authors.value),
      summary.map(ArticleSummary(_)),
      url.map(ArticleUrl(_)),
      source.map(ArticleSource(_)),
      tags.map(_.toSet[String].map(_.toLowerCase.replaceAll(" ", "-"))).map(ArticleTags(_))
    )
  }

  final case class CreateArticleResponse(id: String)

  final case class UpdateArticleRequest(
      title: NonEmptyString,
      content: NonEmptyString,
      createdAt: LocalDate,
      language: NonEmptyString,
      authors: NonEmptyString,
      summary: Option[String],
      url: Option[String],
      source: Option[String],
      tags: Option[List[String]]
  ) {
    def toDomain(id: String): Article =
      Article(
        ArticleId(id),
        ArticleTitle(title.value),
        ArticleContent(content.value),
        ArticleCreatedAt(createdAt),
        ArticleLanguage(language.value),
        ArticleAuthors(authors.value),
        summary.map(ArticleSummary(_)),
        url.map(ArticleUrl(_)),
        source.map(ArticleSource(_)),
        tags.map(_.toSet[String].map(_.toLowerCase.replaceAll(" ", "-"))).map(ArticleTags(_))
      )
  }

  final case class ArticleView(
      id: String,
      title: String,
      content: String,
      createdAt: LocalDate,
      language: String,
      authors: String,
      summary: Option[String],
      url: Option[String],
      source: Option[String],
      tags: Option[Set[String]]
  )

  object ArticleView {
    def from(article: Article): ArticleView = ArticleView(
      article.id.value,
      article.title.value,
      article.content.value,
      article.createdAt.value,
      article.language.value,
      article.authors.value,
      article.summary.map(_.value),
      article.url.map(_.value),
      article.source.map(_.value),
      article.tags.map(_.value)
    )
  }
}
