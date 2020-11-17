package dodona.backtester.lib.domain

import io.circe.Encoder
import io.circe.generic.semiauto._

case class Trade(
  id: Int,
  price: BigDecimal,
  quantity: BigDecimal,
  quoteQuantity: BigDecimal,
  time: Long,
  symbol: String
)

object Trade {
  lazy implicit val TradeEncoder: Encoder[Trade] = deriveEncoder
}