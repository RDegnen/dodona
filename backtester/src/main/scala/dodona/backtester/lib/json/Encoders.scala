package dodona.backtester.lib.json

import io.circe.Encoder, io.circe.generic.auto._
import io.circe.syntax._
import dodona.backtester.models.CandlestickReturnType
import dodona.backtester.models.BinanceCandlestick
import dodona.backtester.models.KrakenCandlestick

object Encoders {
  lazy implicit val encodeCandlestickReturnType: Encoder[CandlestickReturnType] = Encoder.instance {
    case binanceCandlestick @ BinanceCandlestick(ot,o,h,l,c,v,ct) => {
      (ot,o,h,l,c,v,ct).asJson
    }
    case krakenCandlestick @ KrakenCandlestick() => krakenCandlestick.asJson
  }
}