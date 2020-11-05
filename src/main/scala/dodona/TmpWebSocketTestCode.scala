package dodona

import scala.util.{Failure, Success}

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.ws.Message
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Keep, Sink, Source}
import dodona.constants.BinanceConstants.{API_BASE_URL, WS_RAW_STREAM_BASE_URL}
import dodona.constants.KrakenConstants.{KRAKEN_API_BASE_URL, KRAKEN_PRIVATE_WS_URL}
import dodona.constants.{Exchanges, RequestTypes}
import dodona.domain.binance.{BinanceListenKey, BinanceWsMessage}
import dodona.domain.kraken.{KrakenResponse, KrakenWsMessage, KrakenWsSubscription, KrakenWsToken}
import dodona.http.HttpClient
import dodona.http.mappers.DodonaEnpoints
import dodona.websocket.WebSocketClient
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