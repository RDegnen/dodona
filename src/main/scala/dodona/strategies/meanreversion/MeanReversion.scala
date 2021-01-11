package dodona.strategies.meanreversion

import scala.concurrent.ExecutionContext

import akka.actor.typed.ActorSystem
import org.ta4j.core.indicators.EMAIndicator
import org.ta4j.core.indicators.helpers.ClosePriceIndicator
import org.ta4j.core.trading.rules.{
  CrossedDownIndicatorRule,
  CrossedUpIndicatorRule
}
import org.ta4j.core.BaseStrategy
import dodona.MainSystem
import akka.actor.typed.ActorRef
import dodona.events.EventQueue
import dodona.data.BaseDataHandler

/**
  * This class is purly for testing and figuring out how
  * a strategy class should work.
  *
  * @param system
  * @param ec
  */
class MeanReversion(
    implicit
    val system: ActorSystem[MainSystem.Protocol],
    ec: ExecutionContext
) {
  private var strategy: BaseStrategy = _
  // For testing
  private var entered = false
  private var exited = true
  private var dataHandler: BaseDataHandler = _

  def initialize(dh: BaseDataHandler, eq: ActorRef[EventQueue.Event]): Unit = {
    dataHandler = dh
    val series = dh.series
    val closePriceIndicator = new ClosePriceIndicator(series)
    val shortEma = new EMAIndicator(closePriceIndicator, 7)
    val longEma = new EMAIndicator(closePriceIndicator, 25)
    val entryRule = new CrossedUpIndicatorRule(shortEma, longEma)
    val exitRule = new CrossedDownIndicatorRule(shortEma, longEma)
    strategy = new BaseStrategy("Cross", entryRule, exitRule)
  }

  def checkEntryOrExit(): Unit = {
    val series = dataHandler.series
    val endIndex = series.getEndIndex()
    val lastBar = series.getBar(endIndex)
    if (strategy.shouldEnter(endIndex)) {
      if (!entered) {
        if (exited) {
          println(
            s"Entering at ${lastBar.getClosePrice()} - ${lastBar.getEndTime()}"
          )
          entered = true
          exited = false
        }
      }
    } else if (strategy.shouldExit(endIndex)) {
      if (!exited) {
        if (entered) {
          println(
            s"Exiting at ${lastBar.getClosePrice()} - ${lastBar.getEndTime()}"
          )
          entered = false
          exited = true
        }
      }
    }
  }
}
