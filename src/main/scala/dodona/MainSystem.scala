package dodona

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object MainSystem {
  sealed trait Protocol
  final case class GetEventHandler(replyTo: ActorRef[Reply]) extends Protocol

  sealed trait Reply
  final case class EventHandlerReply(actor: ActorRef[EventHandler.Protocol]) extends Reply

  def apply(): Behavior[Protocol] =
    Behaviors.setup(ctx => {
      val eventHandler = ctx.spawn(EventHandler(), "event-handler")

      Behaviors.receiveMessage(msg => {
        msg match {
          case GetEventHandler(replyTo) => 
            replyTo ! EventHandlerReply(eventHandler)
            Behaviors.same
        }
      })
    })
}
