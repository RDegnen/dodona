package dodona.events

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import dodona.execution.IExecutionHandler
import dodona.portfolio.IPortfolio
import dodona.strategy.IStrategy

object EventHandler {
  sealed trait Protocol
  final case object MarketEvent extends Protocol
  final case class SignalEvent(pair: String, price: BigDecimal, side: String) extends Protocol
  final case class NewOrderEvent(
      pair: String,
      orderType: String,
      quantity: BigDecimal,
      side: String
  ) extends Protocol
  final case class OrderUpdateEvent(
      pair: String,
      action: String,
      status: String,
      price: BigDecimal,
      quantity: BigDecimal,
      transactionTime: Long
  ) extends Protocol

  object OrderStatuses {
    val TRADE = "TRADE"
    val CANCELED = "CANCELED"
  }

  def apply(strategy: IStrategy, portfolio: IPortfolio, executionHandler: IExecutionHandler): Behavior[Protocol] =
    Behaviors.setup(ctx => {
      new EventHandler(ctx, strategy, portfolio, executionHandler)
    })
}

class EventHandler(
    ctx: ActorContext[EventHandler.Protocol],
    strategy: IStrategy,
    portfolio: IPortfolio,
    executionHandler: IExecutionHandler
) extends AbstractBehavior[EventHandler.Protocol](ctx) {
  import EventHandler._

  override def onMessage(
      msg: EventHandler.Protocol
  ): Behavior[EventHandler.Protocol] =
    msg match {
      case MarketEvent =>
        strategy.calculateSignals
        this
      case SignalEvent(pair, price, side) =>
        portfolio.updateSignal(pair, price, side)
        this
      case NewOrderEvent(pair, orderType, quantity, side) =>
        executionHandler.executeOrder(pair, orderType, quantity, side)
        this
      case OrderUpdateEvent(pair, action, status, price, quantity, transactionTime) =>
        status match {
          case OrderStatuses.TRADE =>
            portfolio.updateFill(pair, action, status, price, quantity, transactionTime)
          case OrderStatuses.CANCELED =>
            // do something with a canceled order
            println("Order Canceled")
        }
        this
    }
}
