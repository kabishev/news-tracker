package newstracker.clients.yahoo.domain

final case class ArticleDetails(
    uuid: ArticleUuid,
    authors: ArticleAuthors,
    title: ArticleTitle,
    createdAt: ArticleCreatedAt,
    content: ArticleContent,
    summary: ArticleSummary,
    url: ArticleUrl,
    source: ArticleSource
)
