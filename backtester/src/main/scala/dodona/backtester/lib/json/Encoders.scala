package dodona.backtester.lib.json

import dodona.backtester.lib.db.schema.Spread
import dodona.backtester.models.{BinanceCandlestick, CandlestickReturnType, KrakenCandlestick}
import io.circe.Encoder
import io.circe.generic.auto._
import io.circe.generic.semiauto._
import io.circe.syntax._

object Encoders {
  lazy implicit val encodeCandlestickReturnType: Encoder[CandlestickReturnType] = Encoder.instance {
    case binanceCandlestick @ BinanceCandlestick(ot,o,h,l,c,v,ct) => {
      (ot,o,h,l,c,v,ct).asJson
    }
    case krakenCandlestick @ KrakenCandlestick() => krakenCandlestick.asJson
  }
  lazy implicit val SpreadEncoder: Encoder[Spread] = deriveEncoder
}