package dodona.json.binance

import io.circe.Decoder
import dodona.domain.binance.ServerTime
import io.circe.generic.semiauto._

object Decoders {
  lazy implicit val ServerTimeDecoder: Decoder[ServerTime] = deriveDecoder
}