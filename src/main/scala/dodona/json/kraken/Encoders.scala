package dodona.json.kraken

import io.circe.Encoder
import dodona.domain.kraken._
import io.circe.generic.semiauto._

object Encoders {
  lazy implicit val WebSocketSubscriptionEncoder: Encoder[WebSocketSubscription] = deriveEncoder
  lazy implicit val KrakenWebSocketMessageEncoder: Encoder[KrakenWebSocketMessage] = deriveEncoder

}