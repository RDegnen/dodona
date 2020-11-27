package dodona.backtester.models.account

import dodona.backtester.lib.config.AccountConfig

class WalletModel {
  private val startingBalance = AccountConfig.STARTING_BALANCE
  private var balances = Map(
    "USD" -> startingBalance
  )

  def getBalance(symbol: String): Int = balances.get(symbol) match {
    case Some(value) => value
    case None => 0
  }

  def updateBalance(symbol: String, balance: Int): Unit = {
    balances += (symbol -> balance)
  }
}
