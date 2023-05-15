package newstracker.ws

import newstracker.kafka.event._

sealed trait WsEvent

object WsEvent {
  final case class ArticleCreated(articleId: String)                      extends WsEvent
  final case class ArticleTranslated(articleId: String, language: String) extends WsEvent

  final case class ServiceOnline(id: String, name: String)                                        extends WsEvent
  final case class ServiceOffline(id: String)                                                     extends WsEvent
  final case class ServiceError(id: String, error: String)                                        extends WsEvent
  final case class TaskCompleted(id: String, description: String, duration: Long, result: String) extends WsEvent

  object ArticleCreated {
    def from(event: CreatedArticleEvent): ArticleCreated = ArticleCreated(event.id)
  }

  object ArticleTranslated {
    def from(event: TranslatedEvent): ArticleTranslated =
      ArticleTranslated(
        event.id,
        event.language
      )
  }
}
