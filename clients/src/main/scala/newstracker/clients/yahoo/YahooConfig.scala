package newstracker.clients.yahoo

import scala.concurrent.duration.FiniteDuration

final case class YahooConfig(
    baseUri: String,
    apiHost: String,
    apiKey: String,
    pollInterval: FiniteDuration
)
