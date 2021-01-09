package dodona

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import dodona.lib.http.clients.BacktesterHttpClient
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
  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher

  val httpClient = new BacktesterHttpClient()
  val websocketClient = new WebSocketClient()
  val strategy = new MeanReversion(
    httpClient,
    websocketClient,
    "ETHUSD",
    15
  )

  strategy.run()
}