package dodona.backtester.actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.ActorRef

object Prices {
  sealed trait Protocol
  final case class AdjustPrice(pair: String, price: BigDecimal) extends Protocol
  final case class GetPrice(pair: String, replyTo: ActorRef[PriceValue]) extends Protocol

  final case class PriceValue(value: BigDecimal)

  def apply(): Behavior[Protocol] =
    Behaviors.setup(ctx => {
      new Prices(ctx)
    })
}

class Prices(ctx: ActorContext[Prices.Protocol]) extends AbstractBehavior[Prices.Protocol](ctx) {
  import Prices._
  private var pricesMap: Map[String, BigDecimal] = Map()

  override def onMessage(msg: Prices.Protocol): Behavior[Prices.Protocol] = 
    msg match {
      case AdjustPrice(pair, price) => 
        pricesMap += (pair -> price)
        this
      case GetPrice(pair, replyTo) => 
        pricesMap.get(pair) match {
          case Some(value) =>
            replyTo ! PriceValue(value)
            this
          case None =>
            replyTo ! PriceValue(0)
            this
        }
    }
}
