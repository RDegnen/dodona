package dodona.domain.kraken

case class KrakenResponse[R](error: Seq[String], result: R)
case class KrakenServerTime(unixtime: Long, rfc1123: String)