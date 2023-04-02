package newstracker.common

trait Service {
  def isValidId(id: String): Boolean
}
