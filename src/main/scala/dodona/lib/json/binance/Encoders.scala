package dodona.lib.json.binance

import dodona.lib.domain.binance._
import io.circe.Encoder
import io.circe.generic.semiauto._

object Encoders {
  lazy implicit val SymbolEncoder: Encoder[Symbol] = deriveEncoder
  lazy implicit val BinanceWsMessageEncoder: Encoder[BinanceWsMessage] = deriveEncoder
}
