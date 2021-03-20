package dodona.lib.domain.dodona.account

import io.circe.Decoder
import io.circe.generic.semiauto._
import io.circe.Encoder

case class OrderStreamEvent(
    pair: String,
    action: String,
    status: String,
    price: BigDecimal,
    quantity: BigDecimal,
    transactionTime: Long
)

object OrderStreamEvent {
  lazy implicit val OrderFillDecoder: Decoder[OrderStreamEvent] = deriveDecoder
  lazy implicit val OrderFillEncoder: Encoder[OrderStreamEvent] = deriveEncoder
}
