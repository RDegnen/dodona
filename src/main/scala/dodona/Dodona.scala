package dodona

import dodona.constants.BinanceConstants.WS_RAW_STREAM_BASE_URL
import dodona.constants.BinanceConstants.API_BASE_URL
import dodona.constants.KrakenConstants.KRAKEN_API_BASE_URL
import dodona.constants.KrakenConstants.KRAKEN_PRIVATE_WS_URL
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
import dodona.domain.binance.ServerTime
import dodona.domain.kraken.KrakenServerTime
import dodona.json.kraken.Decoders._
import dodona.json.kraken.Encoders._
import dodona.domain.kraken.KrakenResponse
import dodona.constants.Exchanges
import akka.http.scaladsl.model.FormData
import dodona.domain.kraken.WebSocketToken
import dodona.domain.kraken.WebSocketSubscription
import dodona.domain.kraken.KrakenWebSocketMessage
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Keep

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
  val client = new HttpClient(KRAKEN_API_BASE_URL)
  val resposne = client.request[KrakenResponse[WebSocketToken]](
    Exchanges.KRAKEN,
    RequestTypes.SIGNED,
    HttpMethods.POST,
    "/0/private/GetWebSocketsToken",
    Map(),
    headers = Seq(
      RawHeader("API-Key", DodonaConfig.KRAKEN_KEY)
    )
  )

  val (ref, publisher) = Source
    .actorRef[KrakenWebSocketMessage](bufferSize = 100, overflowStrategy = OverflowStrategy.dropBuffer)
    .toMat(Sink.asPublisher(fanout = false))(Keep.both)
    .run()
  
  val source = Source.fromPublisher(publisher)

  val printSink = Sink.foreach[Message](println)
  resposne.onComplete {
    case Success(value)     => {
      val subscription = WebSocketSubscription("ownTrades", value.result.token)
      val message = KrakenWebSocketMessage("subscribe", subscription)

      val (connected, closed) = ws.openSocket[KrakenWebSocketMessage](KRAKEN_PRIVATE_WS_URL, source, printSink)
      connected.onComplete {
        case Success(_) => {
          println("socket open")
          ref ! message
        }
        case Failure(exception) => println(exception)
      }
    }
    case Failure(exception) => println(exception.toString())
  }
}