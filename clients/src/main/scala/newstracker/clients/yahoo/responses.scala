package newstracker.clients.yahoo

import io.circe.generic.semiauto._

import newstracker.clients.yahoo.domain._

import java.time.{Instant, ZoneOffset}

private object responses {
  object NewsList {
    final case class StreamItem(id: String)
    implicit val StreamItemDecoder = deriveDecoder[StreamItem]

    final case class Main(stream: List[StreamItem])
    implicit val MainDecoder = deriveDecoder[Main]

    final case class Data(main: Main)
    implicit val DataDecoder = deriveDecoder[Data]

    final case class Response(data: Data)
    implicit val NewsResponseDecoder = deriveDecoder[Response]
  }

  object NewsGetDetails {
    final case class CanonicalUrl(url: String)
    implicit val CanonicalUrlDecoder = deriveDecoder[CanonicalUrl]

    final case class Provider(displayName: String)
    implicit val ProviderDecoder = deriveDecoder[Provider]

    final case class Body(markup: String)
    implicit val BodyDecoder = deriveDecoder[Body]

    final case class Author(displayName: String)
    implicit val AuthorDecoder = deriveDecoder[Author]

    final case class AuthorElement(author: Author)
    implicit val AuthorElementDecoder = deriveDecoder[AuthorElement]

    final case class Content(
        id: String,
        canonicalUrl: CanonicalUrl,
        title: String,
        authors: List[AuthorElement],
        provider: Provider,
        pubDate: String,
        summary: String,
        body: Body
    )
    implicit val ContentDecoder = deriveDecoder[Content]

    final case class Contents(content: Content)
    implicit val ContentsDecoder = deriveDecoder[Contents]

    final case class Data(contents: List[Contents])
    implicit val DataDecoder = deriveDecoder[Data]

    final case class Response(data: Data) {
      def toArticleDetails: ArticleDetails = {
        val content = data.contents.head.content
        ArticleDetails(
          uuid = ArticleUuid(content.id),
          authors = ArticleAuthors(content.authors.map(_.author.displayName).mkString(", ")),
          title = ArticleTitle(content.title),
          createdAt = ArticleCreatedAt(Instant.parse(content.pubDate).atZone(ZoneOffset.UTC).toLocalDate()),
          content = ArticleContent(content.body.markup),
          summary = ArticleSummary(content.summary),
          url = ArticleUrl(content.canonicalUrl.url),
          source = ArticleSource(content.provider.displayName)
        )
      }
    }
    implicit val NewsResponseDecoder = deriveDecoder[Response]
  }
}
