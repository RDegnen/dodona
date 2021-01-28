package dodona.lib.domain.dodona.market

import io.circe.Decoder
import io.circe.generic.semiauto._

final case class Symbol(symbol: String, filters: List[Filter])

object Symbol {
  lazy implicit val SymbolDecoder: Decoder[Symbol] = deriveDecoder
}