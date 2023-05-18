package newstracker.ws

import newstracker.kafka.event._

sealed trait WsEvent

object WsEvent {
  final case class ArticleCreated(articleId: String)                      extends WsEvent
  final case class ArticleTranslated(articleId: String, language: String) extends WsEvent

  final case class ServiceOnline(id: String, serviceName: String)                      extends WsEvent
  final case class ServiceOffline(id: String, serviceName: String)                     extends WsEvent
  final case class ServiceError(id: String, serviceName: String, error: String)        extends WsEvent
  final case class TaskCompleted(id: String, serviceName: String, description: String) extends WsEvent

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
