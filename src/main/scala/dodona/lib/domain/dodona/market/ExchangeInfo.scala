package dodona.lib.domain.dodona.market

import io.circe.Decoder
import io.circe.generic.semiauto._

final case class ExchangeInfo(symbols: List[Symbol])

object ExchangeInfo {
  lazy implicit val ExchangeInfoDecoder: Decoder[ExchangeInfo] = deriveDecoder
}