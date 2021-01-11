package dodona.events

import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.ActorRef

object EventQueue {
  final case class Event(value: EventHandler.Protocol)

  def apply(eh: ActorRef[EventHandler.Protocol]): Behavior[Event] =
    Behaviors.setup(ctx => {
      new EventQueue(ctx, eh)
    })
}

class EventQueue(ctx: ActorContext[EventQueue.Event], eh: ActorRef[EventHandler.Protocol]) extends AbstractBehavior[EventQueue.Event](ctx) {
  override def onMessage(msg: EventQueue.Event): Behavior[EventQueue.Event] =
    msg match {
      case _ =>
        eh ! msg.value
        this
    }
}