package dodona.backtester.lib.domain

import io.circe.Encoder
import io.circe.generic.semiauto._

case class Order(
  id: Int,
  symbol: String,
  side: String,
  quantity: BigDecimal,
  price: BigDecimal,
  time: Long
)

object Order {
  lazy implicit val OrderEncoder: Encoder[Order] = deriveEncoder
}