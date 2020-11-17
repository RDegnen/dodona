package dodona.lib.domain.dodona.market

import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}

case class Spread(time: Long, bid: BigDecimal, ask: BigDecimal)

object Spread {
  lazy implicit val SpreadEncoder: Encoder[Spread] = deriveEncoder
  lazy implicit val SpreadDecoder: Decoder[Spread] = Decoder.instance { hc =>
    val timeCursor = hc.downArray.right
    for {
      time <- timeCursor.as[Long]
      bidCursor = timeCursor.right
      bid <- bidCursor.as[BigDecimal]
      askCursor = bidCursor.right
      ask <- askCursor.as[BigDecimal]
    } yield Spread(time, bid, ask)
  }
}