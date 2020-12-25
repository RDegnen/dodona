package dodona.backtester.lib.domain

import io.circe.Encoder
import io.circe.generic.semiauto._

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
  lazy implicit val encodeCandlestick: Encoder[Candlestick] = deriveEncoder
}