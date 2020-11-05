package dodona.lib.json.kraken

import dodona.lib.domain.kraken._
import io.circe.Encoder
import io.circe.generic.semiauto._

object Encoders {
  lazy implicit val WebSocketSubscriptionEncoder: Encoder[KrakenWsSubscription] = deriveEncoder
  lazy implicit val KrakenWebSocketMessageEncoder: Encoder[KrakenWsMessage] = deriveEncoder

}