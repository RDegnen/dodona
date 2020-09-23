package dodona

import dodona.constants.BinanceConstants.WS_RAW_STREAM_BASE_URL
import dodona.constants.BinanceConstants.API_BASE_URL
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import dodona.websocket.WebSocketClient
import dodona.domain.binance.WebSocketMessage
import akka.stream.scaladsl.Sink
import akka.http.scaladsl.model.ws.Message
import dodona.http.HttpClient
import dodona.domain.binance.Account
import dodona.constants.RequestTypes
import akka.http.scaladsl.model.HttpMethods
import dodona.json.binance.Decoders._
import akka.http.scaladsl.model.headers.RawHeader
import scala.util.Success
import scala.util.Failure
import dodona.domain.binance.ListenKey

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
  // THIS WORKED FIRST TRY WOOO.
  val ws = new WebSocketClient()
  val client = new HttpClient(API_BASE_URL)
  val getListenKey = client.request[ListenKey](
    RequestTypes.PUBLIC,
    HttpMethods.POST,
    "/api/v3/userDataStream",
    Map(),
    headers = Seq(
      RawHeader("X-MBX-APIKEY", DodonaConfig.BINANCE_US_KEY)
    )
  )

  val printSink = Sink.foreach[Message](println)
  getListenKey.onComplete {
    case Success(value)     => {
      println(value)
      val key = value.listenKey
      ws.openSocket(s"$WS_RAW_STREAM_BASE_URL/$key", printSink)
    }
    case Failure(exception) => println(exception.toString())
  }
}