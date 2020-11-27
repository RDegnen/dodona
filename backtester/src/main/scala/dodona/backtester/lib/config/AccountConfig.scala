package dodona.backtester.lib.config

import com.typesafe.config.ConfigFactory

object AccountConfig {
  val conf = ConfigFactory.load()

  private final val accountParameters =
    conf.getObject("DodonaBacktester.accountParameters").toConfig()
  final val STARTING_BALANCE = accountParameters.getInt("startingBalance")
  final val TRADING_FEE = accountParameters.getDouble("fee")
}
