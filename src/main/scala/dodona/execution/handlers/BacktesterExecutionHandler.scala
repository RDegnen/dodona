package dodona.execution.handlers

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.HttpMethods
import dodona.MainSystem
import dodona.events.{EventHandler, EventQueue}
import dodona.lib.http.clients.BacktesterHttpClient
import dodona.lib.http.{BaseHttpClient, PUBLIC}
import dodona.lib.websocket.IWebSocketClient
import dodona.lib.websocket.WebSocketClient
import dodona.lib.domain.dodona.account.OrderStreamEvent
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
    openSocket[OrderStreamEvent](
      s"$BACKTESTER_WS_URL/order/events",
      onMessage
    )
  }

  def executeOrder(pair: String, orderType: String, quantity: BigDecimal, side: String): Unit = {
    httpClient.generateRequest[String](
      PUBLIC,
      HttpMethods.POST,
      "/order",
      Map("pair" -> pair, "orderType" -> orderType, "quantity" -> quantity.toString, "side" -> side)
    ).onComplete {
      case Failure(exception) => println(s"Exception placing order - $exception")
      case Success(value) =>
    }
  }

  private def onMessage(message: Message): Unit = {
    decode[OrderStreamEvent](message.asTextMessage.getStrictText) match {
      case Right(event) =>
        val orderEvent = EventHandler.OrderUpdateEvent(event.pair, event.action, event.status, event.price, event.quantity, event.transactionTime)
        eventQueue ! EventQueue.Push(orderEvent)
      case Left(err) => println(err)
    }
  }
}
