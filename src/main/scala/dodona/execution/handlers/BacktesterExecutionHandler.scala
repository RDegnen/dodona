package dodona.execution.handlers

import dodona.execution.IExecutionHandler
import dodona.lib.http.{BaseHttpClient, PUBLIC}
import dodona.lib.http.clients.BacktesterHttpClient
import akka.http.scaladsl.model.HttpMethods
import akka.actor.typed.ActorSystem
import scala.concurrent.ExecutionContext
import dodona.MainSystem
import akka.actor.typed.ActorRef
import dodona.events.EventQueue
import dodona.lib.domain.dodona.account.OrderFill
import scala.util.Failure
import scala.util.Success
import dodona.events.EventHandler

class BacktesterExecutionHandler(implicit
    val system: ActorSystem[MainSystem.Protocol],
    ec: ExecutionContext
) extends IExecutionHandler {
  protected val httpClient: BaseHttpClient = new BacktesterHttpClient()
  private var eventQueue: ActorRef[EventQueue.Push] = _

  def initialize(eq: ActorRef[EventQueue.Push]): Unit = {
    eventQueue = eq
  }

  def executeOrder(pair: String, orderType: String, quantity: BigDecimal, side: String): Unit = {
    httpClient.generateRequest[OrderFill](
      PUBLIC,
      HttpMethods.POST,
      "/account/order",
      Map("pair" -> pair, "orderType" -> orderType, "quantity" -> quantity.toString, "side" -> side)
    ).onComplete {
      case Failure(exception) => println(exception)
      case Success(value) =>
        val orderFill = EventHandler.FillEvent(pair, value.action, value.status, value.price, value.quantity, value.transactionTime)
        eventQueue ! EventQueue.Push(orderFill)
    }
  }
}
