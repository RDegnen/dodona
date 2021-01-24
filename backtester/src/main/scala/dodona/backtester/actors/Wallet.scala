package dodona.backtester.actors

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import dodona.backtester.lib.config.AccountConfig

object Wallet {
  sealed trait Protocol
  final case class UpdateAssetBalance(symbol: String, balance: BigDecimal) extends Protocol
  final case class GetBalance(replyTo: ActorRef[BalanceValue]) extends Protocol
  final case class GetAssetBalance(asset: String, replyTo: ActorRef[AssetValue]) extends Protocol

  final case class BalanceValue(balance: Map[String, BigDecimal])
  final case class AssetValue(value: BigDecimal)

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
      case UpdateAssetBalance(symbol, balance) =>
        balances += (symbol -> balance)
        this
      case GetBalance(replyTo) =>
        replyTo ! BalanceValue(balances)
        this
      case GetAssetBalance(asset, replyTo) =>
        balances.get(asset) match {
          case None => 
            replyTo ! AssetValue(0)
            this
          case Some(value) => 
            replyTo ! AssetValue(value)
            this
        }
    }
}