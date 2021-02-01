package dodona.data.handlers

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.ws.Message
import dodona.Constants.BACKTESTER_WS_URL
import dodona.MainSystem
import dodona.data.BaseDataHandler
import dodona.events.{EventHandler, EventQueue}
import dodona.lib.domain.dodona.market.{Candlestick, Trade}
import dodona.lib.http.clients.BacktesterHttpClient
import dodona.lib.http.{BaseHttpClient, PUBLIC}
import dodona.lib.websocket.{IWebSocketClient, WebSocketClient}
import io.circe.parser.decode

class BacktesterDataHandler(pair: String, interval: Int, eq: ActorRef[EventQueue.Push])(implicit
    override val system: ActorSystem[MainSystem.Protocol],
    ec: ExecutionContext
) extends BaseDataHandler(pair, interval) {
  protected val httpClient: BaseHttpClient = new BacktesterHttpClient()
  protected val webSocketClient: IWebSocketClient = new WebSocketClient()

  def initialize(): Unit = {
    getLatestCandlesticks.onComplete {
      case Success(candles) => {
        candles.zipWithIndex.foreach {
          case (candle, idx) => {
            // ADX Is NAN if the first n (20?) bars are dropped.
            // It is NAN because some of the bars have no trades I guess.
            if (idx > 20) {
              addBarToSeries(candle)
            }
          }
        }
        openSocket[Trade](
          s"$BACKTESTER_WS_URL/market/trade?symbol=${pair}&timeToBegin=1569780899999",
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
      Map("symbol" -> pair)
    )

  private def onMessage(message: Message): Unit = {
    decode[Trade](message.asTextMessage.getStrictText) match {
      case Right(trade) => {
        candlestickBuilder.addTrade(trade) match {
          case Some(bar) => {
            addBarToSeries(bar)
            eq ! EventQueue.Push(EventHandler.MarketEvent)
          }
          case None      => series.addTrade(1, trade.price)
        }
      }
      case Left(err) => println(err)
    }
  }
}
