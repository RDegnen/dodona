package dodona.json.binance

import io.circe.Decoder
import dodona.domain.binance._
import io.circe.generic.semiauto._

object Decoders {
  lazy implicit val ServerTimeDecoder: Decoder[ServerTime] = deriveDecoder
  lazy implicit val TickerPriceDecoder: Decoder[TickerPrice] = deriveDecoder
  lazy implicit val AssetBalanceDecoder: Decoder[AssetBalance] = deriveDecoder
  lazy implicit val AccountDecoder: Decoder[Account] = deriveDecoder
}