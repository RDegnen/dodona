package dodona.backtester.models.account

import dodona.backtester.lib.Pairs
import dodona.backtester.lib.config.AccountConfig

class OrderModel {
  private val tradingFee = AccountConfig.TRADING_FEE

  // I want 2 ETH at market price
  // get total order price amount, quantity * market price
  // check to make sure enough USD in account
  // if enough, execute buy
  def placeOrder(symbol: String, quantity: BigDecimal, side: String): Unit = {
    val pair = Pairs.getPair(symbol)

  }
}