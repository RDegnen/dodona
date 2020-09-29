package dodona.domain.kraken

case class KrakenResponse[R](error: Seq[String], result: R)

case class KrakenServerTime(unixtime: Long, rfc1123: String)

case class WebSocketToken(token: String, expires: Int)

case class WebSocketSubscription(name: String, token: String)
case class KrakenWebSocketMessage(event: String, subscription: WebSocketSubscription)