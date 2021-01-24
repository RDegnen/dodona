package dodona.backtester.lib.domain

import io.circe.Encoder
import io.circe.generic.semiauto._

case class OrderFill(
    pair: String,
    action: String,
    status: String,
    price: BigDecimal,
    quantity: BigDecimal,
    transactionTime: Long
)

object OrderFill {
  lazy implicit val OrderFillEncoder: Encoder[OrderFill] = deriveEncoder
}