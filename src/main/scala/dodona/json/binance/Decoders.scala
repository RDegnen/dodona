package dodona.json.binance

import io.circe.Decoder
import dodona.domain.binance._
import dodona.domain.binance.account._
import dodona.domain.binance.market._
import io.circe.generic.semiauto._

object Decoders {
  lazy implicit val ServerTimeDecoder: Decoder[ServerTime] = deriveDecoder
  lazy implicit val ListenKeyDecoder: Decoder[BinanceListenKey] = deriveDecoder

  // Account
  lazy implicit val AssetBalanceDecoder: Decoder[AssetBalance] = deriveDecoder
  lazy implicit val AccountDecoder: Decoder[Account] = deriveDecoder
  
  // Market
  lazy implicit val TickerPriceDecoder: Decoder[TickerPrice] = deriveDecoder
}