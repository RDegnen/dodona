package dodona.backtester.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object MainSystem {
  sealed trait Protocol
  final case class GetPricesActor(replyTo: ActorRef[Reply]) extends Protocol

  sealed trait Reply
  final case class PricesActor(actor: ActorRef[Prices.Protocol]) extends Reply

  def apply(): Behavior[Protocol] =
    Behaviors.receive((ctx, msg) => {
      msg match {
        case GetPricesActor(replyTo) =>
          val prices = ctx.spawn(Prices(), "prices")
          replyTo ! PricesActor(prices)
          Behaviors.same
      }
    })
}
