package newstracker.translation

import newstracker.ApplicationError._
import newstracker.translation.domain.TranslationId

object errors {
  case class TranslationDoesNotExist(id: TranslationId) extends NotFound {
    override def message: String = s"Translation with id ${id.value} does not exist"
  }
}
