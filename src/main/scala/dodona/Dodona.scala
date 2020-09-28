package dodona

import dodona.constants.BinanceConstants.WS_RAW_STREAM_BASE_URL
import dodona.constants.BinanceConstants.API_BASE_URL
import dodona.constants.KrakenConstants.KRAKEN_API_BASE_URL
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
import dodona.domain.kraken.KrakenResponse
import dodona.constants.Exchanges
import akka.http.scaladsl.model.FormData

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

  val client = new HttpClient(KRAKEN_API_BASE_URL)
  val resposne = client.request[KrakenResponse[KrakenServerTime]](
    Exchanges.KRAKEN,
    RequestTypes.SIGNED,
    HttpMethods.POST,
    "/0/private/Balance",
    Map(),
    headers = Seq(
      RawHeader("API-Key", DodonaConfig.KRAKEN_KEY)
    )
  )

  resposne.onComplete {
    case Success(value)     => println(value)
    case Failure(exception) => println(exception.toString())
  }
}