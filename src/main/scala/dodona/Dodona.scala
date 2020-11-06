package dodona

import com.typesafe.config.ConfigFactory
import dodona.constants.{DodonaConstants, Exchanges}
import dodona.lib.http.HttpClient
import dodona.lib.websocket.WebSocketClient
import dodona.strategies.meanreversion.MeanReversion

object DodonaConfig {
  val conf = ConfigFactory.load()

  final val BINANCE_US_KEY = conf.getString("Dodona.binanceUsKey")
  final val BINANCE_US_SECRET = conf.getString("Dodona.binanceUsSecrect")
  final val KRAKEN_KEY = conf.getString("Dodona.krakenKey")
  final val KRAKEN_SECRET = conf.getString("Dodona.krakenSecret")
}

object Dodona extends App {
  val binanceHttpClient = new HttpClient(Exchanges.BACKTESTER, DodonaConstants.BACKTESTER_BASE_URL)
  val binanceWebsocketClient = new WebSocketClient()
  val strategy = new MeanReversion(
    binanceHttpClient,
    binanceWebsocketClient,
    "BTCUSD"
  )

  strategy.run()
}