package dodona.lib.domain.binance.account

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