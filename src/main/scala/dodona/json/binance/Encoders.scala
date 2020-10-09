package dodona.json.binance

import io.circe.Encoder
import dodona.domain.binance._
import io.circe.generic.semiauto._

object Encoders {
  lazy implicit val SymbolEncoder: Encoder[Symbol] = deriveEncoder
  lazy implicit val BinanceWsMessageEncoder: Encoder[BinanceWsMessage] = deriveEncoder
}
