package dodona

import akka.actor.ActorSystem
import dodona.websocket.WebSocketClient
import dodona.http.HttpClient
import dodona.constants.Exchanges
import dodona.domain.kraken.KrakenResponse
import dodona.constants.RequestTypes
import akka.http.scaladsl.model.HttpMethods
import dodona.http.mappers.DodonaEnpoints
import akka.stream.scaladsl.Source
import dodona.domain.kraken.KrakenWsMessage
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Keep
import akka.stream.OverflowStrategy
import akka.http.scaladsl.model.ws.Message
import dodona.domain.binance.BinanceListenKey
import dodona.domain.binance.BinanceWsMessage
import dodona.domain.kraken.KrakenWsToken
import dodona.constants.KrakenConstants.{KRAKEN_API_BASE_URL, KRAKEN_PRIVATE_WS_URL}
import dodona.constants.BinanceConstants.{API_BASE_URL, WS_RAW_STREAM_BASE_URL}
import scala.util.Success
import dodona.domain.kraken.KrakenWsSubscription
import scala.util.Failure
import akka.http.scaladsl.model.headers.RawHeader
/**
  * Temporary file so I can just quickly run this code if I want to for tests.
  * Will remove it soon.
  */
object TmpWebSocketTestCode {
  implicit val system = ActorSystem()
  implicit val executionContext = system.dispatcher

  val krakenTest = () => {
    import dodona.json.kraken.Decoders._
    import dodona.json.kraken.Encoders._
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
    import dodona.json.binance.Decoders._

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