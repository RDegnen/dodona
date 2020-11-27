package dodona.backtester.actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

object Prices {
  sealed trait Protocol
  final case class AdjustPrice(pair: String, price: BigDecimal) extends Protocol

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
        println(pricesMap)
        this
    }
}
