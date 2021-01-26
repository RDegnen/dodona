package dodona.portfolio

import akka.actor.typed.ActorRef
import dodona.data.BaseDataHandler
import dodona.events.EventQueue

trait IPortfolio {
  def initialize(dh: BaseDataHandler, eq: ActorRef[EventQueue.Push]): Unit

  def updateSignal(pair: String, price: BigDecimal, side: String): Unit

  def updateFill(
      pair: String,
      action: String,
      status: String,
      price: BigDecimal,
      quantity: BigDecimal,
      transactionTime: Long
  ): Unit
}
