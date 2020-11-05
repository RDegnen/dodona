package dodona.lib.json.kraken

import dodona.lib.domain.kraken.{KrakenResponse, KrakenServerTime, KrakenWsToken}
import io.circe.Decoder
import io.circe.generic.semiauto._

object Decoders {
  implicit def KrakenResponseDecoder[T: Decoder]: Decoder[KrakenResponse[T]] = deriveDecoder

  lazy implicit val KrakenServerTimeDecoder: Decoder[KrakenServerTime] = deriveDecoder
  lazy implicit val WebSocketTokenDecoder: Decoder[KrakenWsToken] = deriveDecoder
}