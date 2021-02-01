package dodona.lib.domain.dodona.market

import io.circe.Decoder

final case class Candlestick(
  openTime: Long,
  closeTime: Long,
  open: BigDecimal,
  high: BigDecimal,
  low: BigDecimal,
  volume: BigDecimal,
  close: BigDecimal,
)

object Candlestick {
  lazy implicit val CandlestickDecoder: Decoder[Candlestick] = Decoder.instance { cursor =>
    val openTimec = cursor.downArray
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
    } yield Candlestick(openTime, closeTime, open, high, low, volume, close)
  }
}