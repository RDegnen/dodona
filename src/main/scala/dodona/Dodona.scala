package dodona

import com.typesafe.config.ConfigFactory
import dodona.constants.BinanceConstants.API_BASE_URL
import dodona.constants.Exchanges
import dodona.http.HttpClient
import dodona.strategies.meanreversion.MeanReversion
import dodona.websocket.WebSocketClient

object DodonaConfig {
  val conf = ConfigFactory.load()

  final val BINANCE_US_KEY = conf.getString("Dodona.binanceUsKey")
  final val BINANCE_US_SECRET = conf.getString("Dodona.binanceUsSecrect")
  final val KRAKEN_KEY = conf.getString("Dodona.krakenKey")
  final val KRAKEN_SECRET = conf.getString("Dodona.krakenSecret")
}

object Dodona extends App {
  val binanceHttpClient = new HttpClient(Exchanges.BINANCE, API_BASE_URL)
  val binanceWebsocketClient = new WebSocketClient()
  val strategy = new MeanReversion(
    binanceHttpClient,
    binanceWebsocketClient,
    "BTCUSD"
  )

  strategy.run()
}