package dodona.backtester.lib.db.schema

final case class Candlestick(
  id: Int,
  symbol: String,
  interval: String,
  open: BigDecimal,
  high: BigDecimal,
  low: BigDecimal,
  close: BigDecimal,
  volume: BigDecimal,
  openTime: Long,
  closeTime: Long
)