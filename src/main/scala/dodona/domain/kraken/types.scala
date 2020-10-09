package dodona.domain.kraken

case class KrakenResponse[R](error: Seq[String], result: R)

case class KrakenServerTime(unixtime: Long, rfc1123: String)

case class KrakenWsToken(token: String, expires: Int)

case class KrakenWsSubscription(name: String, token: String)
case class KrakenWsMessage(event: String, subscription: KrakenWsSubscription)