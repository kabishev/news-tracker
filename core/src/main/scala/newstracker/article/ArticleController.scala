package newstracker.article

import cats.Monad
import cats.effect.Async
import cats.syntax.applicative._
import cats.syntax.flatMap._
import io.circe.generic.auto._
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

  def routes: HttpRoutes[F] = Http4sServerInterpreter[F].toRoutes(
    List(
      createArticle,
      getAllArticles,
      getArticleById,
      updateArticle
    )
  )

  private val basePath = "articles"

  val idValidator: Validator[String] = Validator.custom(
    id => if (service.isValidId(id)) ValidationResult.Valid else ValidationResult.Invalid(s"Invalid representation of an id: $id"),
    Some(s"Invalid representation of an id")
  )

  private val idPath = basePath / path[String].validate(idValidator).map((s: String) => ArticleId(s))(_.value).name("cat-id")

  private def getAllArticles = Controller.publicEndpoint.get
    .in(basePath)
    .out(jsonBody[List[ArticleView]])
    .serverLogic { _ =>
      service.getAll
        .mapResponse(_.map(ArticleView.from))
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
      Async[F].ensure(req.pure[F])(errors.IdMismatch)(_.id == id.value) >>
        service
          .update(req.toDomain)
          .voidResponse
    }
}

object ArticleController {

  def make[F[_]: Async](service: ArticleService[F]): F[Controller[F]] =
    Monad[F].pure(new ArticleController[F](service))

  final case class CreateArticleRequest(
      title: String,
      content: String,
      createdAt: LocalDate,
      language: String,
      tags: Option[List[String]]
  ) {
    def toDomain: CreateArticle = CreateArticle(
      title,
      content,
      createdAt,
      language,
      tags = tags.map(_.toSet[String].map(_.toLowerCase.replaceAll(" ", "-"))).getOrElse(Set.empty)
    )
  }

  final case class CreateArticleResponse(id: String)

  final case class UpdateArticleRequest(
      id: String,
      title: String,
      content: String,
      createdAt: LocalDate,
      language: String,
      tags: Option[List[String]]
  ) {
    def toDomain: Article =
      Article(
        ArticleId(id),
        title,
        content,
        createdAt,
        language,
        tags = tags.map(_.toSet[String].map(_.toLowerCase.replaceAll(" ", "-"))).getOrElse(Set.empty)
      )
  }

  final case class ArticleView(
      id: String,
      title: String,
      content: String,
      createdAt: LocalDate,
      language: String,
      tags: Set[String]
  )

  object ArticleView {
    def from(article: Article): ArticleView = ArticleView(
      article.id.value,
      article.title,
      article.content,
      article.createdAt,
      article.language,
      article.tags
    )
  }
}
