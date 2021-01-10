package dodona

import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object EventHandler {
  sealed trait Protocol
  final case object MarketEvent extends Protocol

  def apply(): Behavior[Protocol] = 
    Behaviors.setup(ctx => {
      new EventHandler(ctx)
    })
}

class EventHandler(ctx: ActorContext[EventHandler.Protocol]) extends AbstractBehavior[EventHandler.Protocol](ctx) {
  import EventHandler._

  override def onMessage(msg: EventHandler.Protocol): Behavior[EventHandler.Protocol] =
    msg match {
      case MarketEvent =>
        println("New market event")
        this
    }
}