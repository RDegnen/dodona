package dodona.execution.handlers

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.HttpMethods
import dodona.MainSystem
import dodona.events.{EventHandler, EventQueue}
import dodona.lib.domain.dodona.account.OrderFill
import dodona.lib.http.clients.BacktesterHttpClient
import dodona.lib.http.{BaseHttpClient, PUBLIC}
import dodona.lib.websocket.IWebSocketClient
import dodona.lib.websocket.WebSocketClient
import akka.http.scaladsl.model.ws.Message
import io.circe.parser.decode
import dodona.execution.BaseExecutionHandler
import dodona.Constants.BACKTESTER_WS_URL

class BacktesterExecutionHandler(implicit
    override val system: ActorSystem[MainSystem.Protocol],
    ec: ExecutionContext
) extends BaseExecutionHandler {
  protected val httpClient: BaseHttpClient = new BacktesterHttpClient()
  protected val webSocketClient: IWebSocketClient = new WebSocketClient()
  private var eventQueue: ActorRef[EventQueue.Push] = _

  def initialize(eq: ActorRef[EventQueue.Push]): Unit = {
    eventQueue = eq
    openSocket[OrderFill](
      s"$BACKTESTER_WS_URL/account/order_events",
      onMessage
    )
  }

  def executeOrder(pair: String, orderType: String, quantity: BigDecimal, side: String): Unit = {
    httpClient.generateRequest[String](
      PUBLIC,
      HttpMethods.POST,
      "/account/order",
      Map("pair" -> pair, "orderType" -> orderType, "quantity" -> quantity.toString, "side" -> side)
    ).onComplete {
      case Failure(exception) => println(exception)
      case Success(value) =>
    }
  }

  private def onMessage(message: Message): Unit = {
    decode[OrderFill](message.asTextMessage.getStrictText) match {
      case Right(fill) =>
        val orderFill = EventHandler.FillEvent(fill.pair, fill.action, fill.status, fill.price, fill.quantity, fill.transactionTime)
        eventQueue ! EventQueue.Push(orderFill)
      case Left(err) => println(err)
    }
  }
}
