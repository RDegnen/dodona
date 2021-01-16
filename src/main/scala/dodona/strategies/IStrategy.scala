package dodona.strategies

import dodona.data.BaseDataHandler
import akka.actor.typed.ActorRef
import dodona.events.EventQueue

trait IStrategy {
  def initialize(dh: BaseDataHandler, eq: ActorRef[EventQueue.Push]): Unit

  def calculateSignals(): Unit
}
