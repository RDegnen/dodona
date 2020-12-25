package dodona.backtester.actors

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import dodona.backtester.lib.config.AccountConfig

object Wallet {
  sealed trait Protocol
  final case class UpdateBalance(symbol: String, balance: BigDecimal) extends Protocol
  final case class GetBalance(symbol: String, replyTo: ActorRef[BalanceValue]) extends Protocol

  final case class BalanceValue(value: BigDecimal)

  def apply(): Behavior[Protocol] =
    Behaviors.setup(ctx => {
      new Wallet(ctx)
    })
}

class Wallet(ctx: ActorContext[Wallet.Protocol]) extends AbstractBehavior[Wallet.Protocol](ctx) {
  import Wallet._
  private val startingBalance = AccountConfig.STARTING_BALANCE
  private var balances = Map(
    "USD" -> BigDecimal(startingBalance)
  )

  override def onMessage(msg: Wallet.Protocol): Behavior[Wallet.Protocol] =
    msg match {
      case UpdateBalance(symbol, balance) =>
        balances += (symbol -> balance)
        this
      case GetBalance(symbol, replyTo) =>
        balances.get(symbol) match {
          case Some(value) => 
            replyTo ! BalanceValue(value)
            this
          case None => 
            replyTo ! BalanceValue(0)
            this
        }
    }
}