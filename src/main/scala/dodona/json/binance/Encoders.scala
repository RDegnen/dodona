package dodona.json.binance

import io.circe.Encoder
import dodona.domain.binance._
import io.circe.generic.semiauto._

object Encoders {
  lazy implicit val SymbolEncoder: Encoder[Symbol] = deriveEncoder
  lazy implicit val WebSocketMessageEncoder: Encoder[WebSocketMessage] = deriveEncoder
}

// Some examples on how to create json
// val json = """
// {
//   symbol: "VETUSD"
// }
// """
// This uses an Encoder
// val json = Symbol("VETUSD").asJson.toString()