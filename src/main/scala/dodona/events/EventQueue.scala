package dodona.events

import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.ActorRef

object EventQueue {
  final case class Push(value: EventHandler.Protocol)

  def apply(eh: ActorRef[EventHandler.Protocol]): Behavior[Push] =
    Behaviors.setup(ctx => {
      new EventQueue(ctx, eh)
    })
}

class EventQueue(
    ctx: ActorContext[EventQueue.Push],
    eh: ActorRef[EventHandler.Protocol]
) extends AbstractBehavior[EventQueue.Push](ctx) {
  override def onMessage(msg: EventQueue.Push): Behavior[EventQueue.Push] =
    msg match {
      case _ =>
        eh ! msg.value
        this
    }
}
