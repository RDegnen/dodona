package dodona.data.handlers

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.ws.Message
import dodona.Constants.BACKTESTER_WS_URL
import dodona.data.BaseDataHandler
import dodona.lib.domain.dodona.market.{Candlestick, Trade}
import dodona.lib.http.clients.BacktesterHttpClient
import dodona.lib.http.{BaseHttpClient, PUBLIC}
import dodona.lib.websocket.{IWebSocketClient, WebSocketClient}
import io.circe.parser.decode
import dodona.MainSystem
import akka.actor.typed.ActorRef
import dodona.events.EventHandler
import dodona.events.EventQueue

class BacktesterDataHandler(asset: String, interval: Int, eq: ActorRef[EventQueue.Event])(implicit
    override val system: ActorSystem[MainSystem.Protocol],
    ec: ExecutionContext
) extends BaseDataHandler(asset, interval) {
  protected val httpClient: BaseHttpClient = new BacktesterHttpClient()
  protected val webSocketClient: IWebSocketClient = new WebSocketClient()

  def initialize(): Unit = {
    getLatestCandlesticks.onComplete {
      case Success(candles) => {
        candles.foreach(addBarToSeries)
        openSocket[Trade](
          s"$BACKTESTER_WS_URL/trade?symbol=${asset}&timeToBegin=1569222899999",
          onMessage
        )
      }
      case Failure(err) => println(err)
    }
  }

  private def getLatestCandlesticks(): Future[List[Candlestick]] =
    httpClient.generateRequest[List[Candlestick]](
      PUBLIC,
      HttpMethods.GET,
      "/market/OHLC",
      Map("symbol" -> asset)
    )

  private def onMessage(message: Message): Unit = {
    decode[Trade](message.asTextMessage.getStrictText) match {
      case Right(trade) => {
        candlestickBuilder.addTrade(trade) match {
          case Some(bar) => {
            addBarToSeries(bar)
            eq ! EventQueue.Event(EventHandler.MarketEvent)
          }
          case None      => series.addPrice(trade.price)
        }
      }
      case Left(err) => println(err)
    }
  }
}
