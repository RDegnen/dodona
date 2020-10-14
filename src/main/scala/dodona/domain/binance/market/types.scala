package dodona.domain.binance.market

case class HttpCandlestickResponse(
  openTime:                 Long,
  open:                     String,
  high:                     String,
  low:                      String,
  close:                    String,
  volume:                   String,
  closeTime:                Long,
  // quoteAssetVolume:         String,
  // numberOfTrades:           Long,
  // takerBuyBaseAssetVolume:  String,
  // takerBuyQuoteAssetVolume: String
)

case class Candlestick(
  t: Int,
  T: Int,
  s: String,
  i: String,
  f: Int,
  L: Int,
  o: BigDecimal,
  c: BigDecimal,
  h: BigDecimal,
  l: BigDecimal,
  v: String,
  n: Int,
  x: Boolean,
  q: BigDecimal,
  V: String,
  Q: BigDecimal,
  B: String
)

case class KlineCandlestickInterval(
  e: String,
  E: Int,
  s: String,
  k: Candlestick
)
