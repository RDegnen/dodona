package dodona.backtester.lib.domain

import io.circe.Encoder
import io.circe.syntax._

case class Candlestick(
    openTime: Long,
    open: BigDecimal,
    high: BigDecimal,
    low: BigDecimal,
    close: BigDecimal,
    volume: BigDecimal,
    closeTime: Long
)

object Candlestick {
  lazy implicit val encodeCandlestick: Encoder[Candlestick] = Encoder.instance {
    case candlestick @ Candlestick(ot,o,h,l,c,v,ct) => (ot,o,h,l,c,v,ct).asJson
  }
}