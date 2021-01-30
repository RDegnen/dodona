package dodona.lib.domain.dodona.account

import io.circe.Decoder
import io.circe.generic.semiauto._
import io.circe.Encoder

case class OrderFill(
    pair: String,
    action: String,
    status: String,
    price: BigDecimal,
    quantity: BigDecimal,
    transactionTime: Long
)

object OrderFill {
  lazy implicit val OrderFillDecoder: Decoder[OrderFill] = deriveDecoder
  lazy implicit val OrderFillEncoder: Encoder[OrderFill] = deriveEncoder
}