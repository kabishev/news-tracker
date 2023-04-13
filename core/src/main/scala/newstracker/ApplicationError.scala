package newstracker

sealed trait ApplicationError extends Throwable {
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
