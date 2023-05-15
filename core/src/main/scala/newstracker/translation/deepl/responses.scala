package newstracker.translation.deepl

import io.circe.Decoder
import io.circe.generic.semiauto._

private object responses {
  object Translate {
    case class Translation(text: String)
    implicit val translationDecoder: Decoder[Translation] = deriveDecoder[Translation]

    case class Response(translations: Seq[Translation])
    implicit val responseDecoder: Decoder[Response] = deriveDecoder[Response]
  }
}
