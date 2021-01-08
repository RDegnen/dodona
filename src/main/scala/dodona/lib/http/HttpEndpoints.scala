package dodona.lib.http

final case class HttpEndpoint(value: String)

object HttpEndpoints {
  final val CANDLESTICKS = HttpEndpoint("CANDLESTICKS")
  final val WEBSOCKET_TOKEN = HttpEndpoint("WEBSOCKET_TOKEN")
}