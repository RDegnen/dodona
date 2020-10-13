package dodona

import dodona.constants.BinanceConstants.WS_RAW_STREAM_BASE_URL
import dodona.constants.BinanceConstants.API_BASE_URL
import dodona.constants.KrakenConstants.KRAKEN_API_BASE_URL
import dodona.constants.KrakenConstants.KRAKEN_PRIVATE_WS_URL
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import dodona.websocket.WebSocketClient
import dodona.domain.binance.BinanceWsMessage
import akka.stream.scaladsl.Sink
import akka.http.scaladsl.model.ws.Message
import dodona.http.HttpClient
import dodona.constants.RequestTypes
import akka.http.scaladsl.model.HttpMethods
import dodona.json.binance.Decoders._
import akka.http.scaladsl.model.headers.RawHeader
import scala.util.Success
import scala.util.Failure
import dodona.domain.binance.BinanceListenKey
import dodona.domain.binance.ServerTime
import dodona.domain.kraken.KrakenServerTime
import dodona.json.kraken.Decoders._
import dodona.json.kraken.Encoders._
import dodona.domain.kraken.KrakenResponse
import dodona.constants.Exchanges
import akka.http.scaladsl.model.FormData
import dodona.domain.kraken.KrakenWsToken
import dodona.domain.kraken.KrakenWsSubscription
import dodona.domain.kraken.KrakenWsMessage
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Keep
import dodona.http.mappers.DodonaEnpoints

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

  val krakenTest = () => {
    val ws = new WebSocketClient()
    val client = new HttpClient(Exchanges.KRAKEN, KRAKEN_API_BASE_URL)
    val resposne = client.request[KrakenResponse[KrakenWsToken]](
      RequestTypes.SIGNED,
      HttpMethods.POST,
      DodonaEnpoints.WEBSOCKET_TOKEN
    )

    val (ref, publisher) = Source
      .actorRef[KrakenWsMessage](bufferSize = 100, overflowStrategy = OverflowStrategy.dropBuffer)
      .toMat(Sink.asPublisher(fanout = false))(Keep.both)
      .run()
    
    val source = Source.fromPublisher(publisher)

    val printSink = Sink.foreach[Message](println)
    resposne.onComplete {
      case Success(value)     => {
        val subscription = KrakenWsSubscription("ownTrades", value.result.token)
        val message = KrakenWsMessage("subscribe", subscription)

        val (connected, closed) = ws.openSocket[KrakenWsMessage](KRAKEN_PRIVATE_WS_URL, source, printSink)
        connected.onComplete {
          case Success(_) => {
            println("kraken socket open")
            ref ! message
          }
          case Failure(exception) => println(exception)
        }
      }
      case Failure(exception) => println(exception.toString())
    }
  }

  val binanceTest = () => {
    import dodona.json.binance.Encoders._

    val ws = new WebSocketClient()
    val client = new HttpClient(Exchanges.BINANCE, API_BASE_URL)
    val getListenKey = client.request[BinanceListenKey](
      RequestTypes.PUBLIC,
      HttpMethods.POST,
      DodonaEnpoints.WEBSOCKET_TOKEN,
      headers = Seq(RawHeader("X-MBX-APIKEY", DodonaConfig.BINANCE_US_KEY))
    )

    val (ref, publisher) = Source
      .actorRef[BinanceWsMessage](bufferSize = 100, overflowStrategy = OverflowStrategy.dropBuffer)
      .toMat(Sink.asPublisher(fanout = false))(Keep.both)
      .run()
    
    val source = Source.fromPublisher(publisher)

    val printSink = Sink.foreach[Message](println)
    getListenKey.onComplete {
      case Success(value) => {
        val key = value.listenKey
        val (connected, closed) = ws.openSocket[BinanceWsMessage](s"$WS_RAW_STREAM_BASE_URL/$key", source, printSink)

        connected.onComplete {
          case Success(_) => {
            println("binance socket open")
          }
          case Failure(exception) => println(exception)
        }
      }
    }
  }

  binanceTest()
}