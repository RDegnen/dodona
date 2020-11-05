package dodona.backtester

import com.typesafe.config.ConfigFactory
import dodona.backtester.lib.http.HttpClient
import dodona.backtester.lib.websocket.WebSocketClient
import dodona.constants.{BinanceConstants, Exchanges}
import dodona.strategies.meanreversion.MeanReversion

object BacktesterConfig {
  val conf = ConfigFactory.load()
}

object Backtester extends App {
  val httpClient = new HttpClient(Exchanges.BINANCE, BinanceConstants.API_BASE_URL)
  val webSocketClient = new WebSocketClient()
  val strategy = new MeanReversion(httpClient, webSocketClient, "BTCUSD")

  strategy.run()
}
