package newstracker.clients.yahoo

import scala.concurrent.duration.FiniteDuration

final case class YahooConfig(
    nameSuffix: String,
    baseUri: String,
    apiHost: String,
    apiKey: String,
    region: String,
    pollInterval: FiniteDuration
)
