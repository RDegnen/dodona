package dodona.strategies.meanreversion

import dodona.http.IHttpClient
import dodona.websocket.IWebSocketClient
import dodona.http.mappers.DodonaEnpoints
import dodona.constants.RequestTypes
import akka.http.scaladsl.model.HttpMethods
import dodona.json.binance.Decoders._
import dodona.domain.dodona.http.CandlestickParams
import scala.util.Success
import scala.util.Failure
import akka.actor.ActorSystem
import dodona.domain.binance.market.HttpCandlestickResponse

class MeanReversion(
    val httpClient: IHttpClient,
    val websocketClient: IWebSocketClient,
    val pair: String
) {
  implicit val system = ActorSystem()
  implicit val executionContext = system.dispatcher
  
  def start(): Unit = {
    val candlesticks = httpClient.request[List[HttpCandlestickResponse]](
      RequestTypes.PUBLIC,
      HttpMethods.GET,
      DodonaEnpoints.CANDLESTICKS,
      CandlestickParams(pair, "1m")
    )

    candlesticks.onComplete {
      case Success(value) => println(value)
      case Failure(exception) => println(exception)
    }
  }
}
