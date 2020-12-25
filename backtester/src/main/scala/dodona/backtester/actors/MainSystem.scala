package dodona.backtester.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object MainSystem {
  sealed trait Protocol
  final case class GetPricesActor(replyTo: ActorRef[Reply]) extends Protocol
  final case class GetWalletActor(replyTo: ActorRef[Reply]) extends Protocol

  sealed trait Reply
  final case class PricesActor(actor: ActorRef[Prices.Protocol]) extends Reply
  final case class WalletActor(actor: ActorRef[Wallet.Protocol]) extends Reply

  def apply(): Behavior[Protocol] =
    Behaviors.setup(ctx => {
      val prices = ctx.spawn(Prices(), "prices")
      val wallet = ctx.spawn(Wallet(), "wallet")
      
      Behaviors.receiveMessage(msg => {
        msg match {
          case GetPricesActor(replyTo) =>
            replyTo ! PricesActor(prices)
            Behaviors.same
          case GetWalletActor(replyTo) => 
            replyTo ! WalletActor(wallet)
            Behaviors.same
        }
      })
    })
}
