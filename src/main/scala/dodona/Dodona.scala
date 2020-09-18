package dodona

import dodona.constants.BinanceConstants.WS_RAW_STREAM_BASE_URL
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import dodona.websocket.WebSocketClient
import dodona.domain.binance.WebSocketMessage
import akka.stream.scaladsl.Sink
import akka.http.scaladsl.model.ws.Message

object DodonaConfig {
  val conf = ConfigFactory.load()

  final val BINANCE_US_KEY = conf.getString("Dodona.binanceUsKey")
  final val BINANCE_US_SECRET = conf.getString("Dodona.binanceUsSecrect")
  final val KRAKEN_KEY = conf.getString("Dodona.krakenKey")
  final val KRAKEN_SECRET = conf.getString("Dodona.krakenSecret")
}

object Dodona extends App {
  implicit val system = ActorSystem()
  implicit val executionContext = system.dispatcher

  val ws = new WebSocketClient()
  val printSink = Sink.foreach[Message](println)
  val socket = ws.openSocketWithMessage(
    WS_RAW_STREAM_BASE_URL,
    WebSocketMessage("SUBSCRIBE", List("vetusd@ticker"), 1),
    printSink
  )
}