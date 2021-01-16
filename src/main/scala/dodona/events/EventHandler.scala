package dodona.events

import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import dodona.strategies.IStrategy

object EventHandler {
  sealed trait Protocol
  final case object MarketEvent extends Protocol
  final case class SignalEvent(price: BigDecimal, side: String) extends Protocol

  def apply(strategy: IStrategy): Behavior[Protocol] =
    Behaviors.setup(ctx => {
      new EventHandler(ctx, strategy)
    })
}

class EventHandler(
    ctx: ActorContext[EventHandler.Protocol],
    strategy: IStrategy
) extends AbstractBehavior[EventHandler.Protocol](ctx) {
  import EventHandler._

  override def onMessage(
      msg: EventHandler.Protocol
  ): Behavior[EventHandler.Protocol] =
    msg match {
      case MarketEvent =>
        strategy.calculateSignals
        this
      case SignalEvent(price, side) =>
        this
    }
}
