package dodona

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import dodona.events.EventQueue
import dodona.events.EventHandler
import dodona.strategies.IStrategy

object MainSystem {
  sealed trait Protocol
  final case class InitEvents(replyTo: ActorRef[Reply], strategy: IStrategy) extends Protocol

  sealed trait Reply
  final case class EventQueueReply(actor: ActorRef[EventQueue.Push]) extends Reply

  def apply(): Behavior[Protocol] =
    Behaviors.setup(ctx => {
      Behaviors.receiveMessage(msg => {
        msg match {
          case InitEvents(replyTo, strategy) =>
            val eh = ctx.spawn(EventHandler(strategy), "event-handler")
            val eq = ctx.spawn(EventQueue(eh), "event-queue")
            replyTo ! EventQueueReply(eq)
            Behaviors.same
        }
      })
    })
}
