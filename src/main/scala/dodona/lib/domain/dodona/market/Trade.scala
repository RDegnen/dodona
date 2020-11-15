package dodona.lib.domain.dodona.market

import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}

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
  lazy implicit val TradeDecoder: Decoder[Trade] = deriveDecoder
}