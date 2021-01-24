package dodona.execution

import akka.actor.typed.ActorRef
import dodona.events.EventQueue

trait IExecutionHandler {
  def initialize(eq: ActorRef[EventQueue.Push]): Unit

  def executeOrder(pair: String, orderType: String, quantity: BigDecimal, side: String): Unit
}
