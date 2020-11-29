package dodona.backtester.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object MainSystem {
  sealed trait Protocol
  final case class GetPricesActor(replyTo: ActorRef[Reply]) extends Protocol

  sealed trait Reply
  final case class PricesActor(actor: ActorRef[Prices.Protocol]) extends Reply

  def apply(): Behavior[Protocol] =
    Behaviors.setup(ctx => {
      val prices = ctx.spawn(Prices(), "prices")
      
      Behaviors.receiveMessage(msg => {
        msg match {
          case GetPricesActor(replyTo) =>
            replyTo ! PricesActor(prices)
            Behaviors.same
        }
      })
    })
}
