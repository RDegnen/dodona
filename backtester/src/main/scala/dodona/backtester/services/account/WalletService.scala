package dodona.backtester.services.account

import dodona.backtester.lib.config.AccountConfig

/**
  * This is a service right now because it holds some state
  * and does not interact with the database. I don't think it
  * needs to be an actor.
  */
object WalletService {
  private val startingBalance = AccountConfig.STARTING_BALANCE
  private var balances = Map(
    "USD" -> BigDecimal(startingBalance)
  )

  def getBalance(symbol: String): BigDecimal = balances.get(symbol) match {
    case Some(value) => value
    case None => 0
  }

  def updateBalance(symbol: String, balance: BigDecimal): Unit = {
    balances += (symbol -> balance)
  }
}
