package newstracker.common

import newstracker.ApplicationError._

object errors {
  final case class FailedValidation(message: String) extends Unprocessable
}
