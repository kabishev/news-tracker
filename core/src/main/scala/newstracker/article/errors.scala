package newstracker.article

import newstracker.ApplicationError._
import newstracker.article.domain.ArticleId

object errors {
  case class ArticleDoesNotExist(id: ArticleId) extends NotFound {
    override def message: String = s"Article with id ${id.value} does not exist"
  }

  case object IdMismatch extends BadRequest {
    override val message: String = "The id supplied in the path does not match with the id in the request body"
  }
}
