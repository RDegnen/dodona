package dodona.lib.domain.binance.market

case class Candlestick(
  openTime:                 Long,
  open:                     BigDecimal,
  high:                     BigDecimal,
  low:                      BigDecimal,
  close:                    BigDecimal,
  volume:                   BigDecimal,
  closeTime:                Long,
  // quoteAssetVolume:         String,
  // numberOfTrades:           Long,
  // takerBuyBaseAssetVolume:  String,
  // takerBuyQuoteAssetVolume: String
)

// WebSocket candles
case class KlineCandlestick(
  t: Long,
  T: Long,
  s: String,
  i: String,
  f: Long,
  L: Long,
  o: BigDecimal,
  c: BigDecimal,
  h: BigDecimal,
  l: BigDecimal,
  v: BigDecimal,
  n: Long,
  x: Boolean,
  q: BigDecimal,
  V: String,
  Q: BigDecimal,
  B: String
)

case class KlineCandlestickInterval(
  e: String,
  E: Long,
  s: String,
  k: KlineCandlestick
)
