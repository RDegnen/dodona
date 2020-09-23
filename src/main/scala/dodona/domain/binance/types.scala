package dodona.domain.binance

case class ServerTime(serverTime: Long)

case class TickerPrice(symbol: String, price: BigDecimal)

case class Symbol(symbol: String)

case class AssetBalance(
  asset: String,
  free: BigDecimal,
  locked: BigDecimal
)

case class Account(
  makerCommission: Int,
  takerCommission: Int,
  buyerCommission: Int,
  sellerCommission: Int,
  canTrade: Boolean,
  canWithdraw: Boolean,
  canDeposit: Boolean,
  updateTime: BigDecimal,
  balances: List[AssetBalance]
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

case class WebSocketMessage(method: String, params: List[String], id: Int)

case class ListenKey(listenKey: String)