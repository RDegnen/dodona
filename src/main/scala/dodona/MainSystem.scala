package dodona

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import dodona.events.{EventHandler, EventQueue}
import dodona.execution.IExecutionHandler
import dodona.portfolio.IPortfolio
import dodona.strategies.IStrategy

object MainSystem {
  sealed trait Protocol
  final case class InitEvents(
      replyTo: ActorRef[Reply],
      strategy: IStrategy,
      portfolio: IPortfolio,
      executionHandler: IExecutionHandler
  ) extends Protocol

  sealed trait Reply
  final case class EventQueueReply(actor: ActorRef[EventQueue.Push]) extends Reply

  def apply(): Behavior[Protocol] =
    Behaviors.setup(ctx => {
      Behaviors.receiveMessage(msg => {
        msg match {
          case InitEvents(replyTo, strategy, portfolio, executionHandler) =>
            val eh = ctx.spawn(
              EventHandler(strategy, portfolio, executionHandler),
              "event-handler"
            )
            val eq = ctx.spawn(EventQueue(eh), "event-queue")
            replyTo ! EventQueueReply(eq)
            Behaviors.same
        }
      })
    })
}
