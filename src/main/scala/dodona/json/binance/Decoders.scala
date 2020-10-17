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
  lazy implicit val CandlestickDecoder: Decoder[KlineCandlestick] = deriveDecoder
  lazy implicit val KlineCandlestickIntervalDecoder: Decoder[KlineCandlestickInterval] = deriveDecoder
  lazy implicit val HttpCandlestickResponseDecoder: Decoder[Candlestick] =
    Decoder.instance { c =>
      // This decodes a list of HttpCandleStickResponses,
      // but not a single one. Still figuring out circe...
      val openTimec = c.downArray
      for {
        openTime <- openTimec.as[Long]
        openc = openTimec.right
        open <- openc.as[BigDecimal]
        highc = openc.right
        high <- highc.as[BigDecimal]
        lowc = highc.right
        low <- lowc.as[BigDecimal]
        closec = lowc.right
        close <- closec.as[BigDecimal]
        volumec = closec.right
        volume <- volumec.as[BigDecimal]
        closeTimec = volumec.right
        closeTime <- closeTimec.as[Long]
      } yield Candlestick(openTime, open, high, low, close, volume, closeTime)
    }
}