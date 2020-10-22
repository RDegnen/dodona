package dodona.backtester

import com.typesafe.config.ConfigFactory
import dodona.backtester.models.CandlestickModel
import akka.actor.ActorSystem
import dodona.backtester.controllers.CandlesticksController
import dodona.constants.Exchanges
import scala.util.Success
import scala.util.Failure
import dodona.backtester.http.HttpClient
import dodona.constants.BinanceConstants
import dodona.domain.binance.market.Candlestick
import dodona.constants.RequestTypes
import akka.http.scaladsl.model.HttpMethods
import dodona.http.mappers.DodonaEnpoints
import dodona.domain.dodona.http.CandlestickParams
import dodona.json.binance.Decoders._

object BacktesterConfig {
  val conf = ConfigFactory.load()
}

object Backtester extends App {
  implicit val system = ActorSystem()
  implicit val executionContext = system.dispatcher

  val client = new HttpClient(Exchanges.BINANCE, BinanceConstants.API_BASE_URL)
  val response = client.request[List[Candlestick]](
    RequestTypes.PUBLIC,
    HttpMethods.GET,
    DodonaEnpoints.CANDLESTICKS,
    CandlestickParams("BTCUSD", "15m")
  )
  
  response.onComplete {
    case Success(value) => println(value)
    case Failure(exception) => println(exception)
  }
}
