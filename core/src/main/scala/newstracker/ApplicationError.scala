package newstracker

import scala.util.control.NoStackTrace

sealed trait ApplicationError extends NoStackTrace {
  // NoStackTrace nice optimization stuff when you don't need stacktrace,
  // because you apriori know where error was thrown
  def message: String
  override def getMessage: String = message
}

object ApplicationError {
  trait NotFound      extends ApplicationError
  trait Conflict      extends ApplicationError
  trait BadRequest    extends ApplicationError
  trait Forbidden     extends ApplicationError
  trait Unprocessable extends ApplicationError
}
