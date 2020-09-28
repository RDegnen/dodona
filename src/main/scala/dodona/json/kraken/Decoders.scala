package dodona.json.kraken

import io.circe.Decoder
import dodona.domain.kraken.KrakenServerTime
import io.circe.generic.semiauto._
import dodona.domain.kraken.KrakenResponse

object Decoders {
  implicit def KrakenResponseDecoder[T: Decoder]: Decoder[KrakenResponse[T]] = deriveDecoder

  lazy implicit val KrakenServerTimeDecoder: Decoder[KrakenServerTime] = deriveDecoder
}