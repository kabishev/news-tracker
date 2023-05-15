package newstracker.ws

import newstracker.kafka.event._

sealed trait WsEvent

object WsEvent {
  final case class ArticleCreated(articleId: String) extends WsEvent

  object ArticleCreated {
    def from(event: CreatedArticleEvent): ArticleCreated = ArticleCreated(event.id)
  }

  final case class ArticleTranslated(
      articleId: String,
      language: String
  ) extends WsEvent

  object ArticleTranslated {
    def from(event: TranslatedEvent): ArticleTranslated =
      ArticleTranslated(
        event.id,
        event.language
      )
  }
}
