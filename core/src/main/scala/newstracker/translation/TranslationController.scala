package newstracker.translation

import cats.Monad
import cats.effect.Async
import io.circe.generic.auto._
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.http4s.Http4sServerInterpreter

import newstracker.common.Controller
import newstracker.translation.domain.{Translation, _}

final private class TranslationController[F[_]: Async](
    private val service: TranslationService[F]
) extends Controller[F] {
  import TranslationController._

  override def routes = _ =>
    Http4sServerInterpreter[F](Controller.serverOptions[F]).toRoutes(
      List(
        getTranslationById,
        createLocalization
      )
    )

  private val basePath = "translations"

  private val idValidator: Validator[String] = Validator.custom(
    id => if (service.isValidId(id)) ValidationResult.Valid else ValidationResult.Invalid(s"Invalid representation of an id: $id"),
    Some(s"Invalid representation of an id")
  )

  private val idPath = basePath / path[String].validate(idValidator).map((s: String) => TranslationId(s))(_.value).name("translation-id")
  private val languagePath = path[String].validate(Validator.pattern("^[a-z]{2}$")).map(LocalizationLanguage(_))(_.value).name("language")

  private def getTranslationById = Controller.publicEndpoint.get
    .in(idPath)
    .out(jsonBody[TranslationView])
    .serverLogic { id =>
      service
        .get(id)
        .mapResponse(TranslationView.from)
    }

  private def createLocalization = Controller.publicEndpoint.post
    .in(idPath / "localizations" / languagePath)
    .serverLogic { case (id, language) =>
      service
        .createLocalization(CreateLocalization(id, language))
        .voidResponse
    }
}

object TranslationController {

  def make[F[_]: Async](
      service: TranslationService[F]
  ): F[Controller[F]] =
    Monad[F].pure(new TranslationController[F](service))

  final case class TranslationView(
      id: String,
      localizations: List[LocalizationView]
  )

  object TranslationView {
    def from(translation: Translation): TranslationView = TranslationView(
      id = translation.id.value,
      localizations = translation.localizations.map(LocalizationView.from)
    )
  }

  final case class LocalizationView(
      language: String,
      content: String
  )

  object LocalizationView {
    def from(localization: Localization): LocalizationView = LocalizationView(
      language = localization.language.value,
      content = localization.content.value
    )
  }
}
