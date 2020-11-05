package dodona.json.kraken

import dodona.domain.kraken._
import io.circe.Encoder
import io.circe.generic.semiauto._

object Encoders {
  lazy implicit val WebSocketSubscriptionEncoder: Encoder[KrakenWsSubscription] = deriveEncoder
  lazy implicit val KrakenWebSocketMessageEncoder: Encoder[KrakenWsMessage] = deriveEncoder

}