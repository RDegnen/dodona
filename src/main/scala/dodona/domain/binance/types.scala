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
  updateTime: String,
  balances: List[AssetBalance]
)