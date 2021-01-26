package dodona.strategies.meanreversion

import scala.concurrent.ExecutionContext

import akka.actor.typed.{ActorRef, ActorSystem}
import dodona.data.BaseDataHandler
import dodona.events.{EventHandler, EventQueue}
import dodona.strategies.IStrategy
import dodona.{Constants, MainSystem}
import org.ta4j.core.BaseStrategy
import org.ta4j.core.indicators.EMAIndicator
import org.ta4j.core.indicators.helpers.ClosePriceIndicator
import org.ta4j.core.trading.rules.{
  CrossedDownIndicatorRule,
  CrossedUpIndicatorRule
}

/**
  * This class is purly for testing and figuring out how
  * a strategy class should work.
  *
  * @param system
  * @param ec
  */
class MeanReversion(implicit
    val system: ActorSystem[MainSystem.Protocol],
    ec: ExecutionContext
) extends IStrategy {
  private var strategy: BaseStrategy = _
  private var dataHandler: BaseDataHandler = _
  private var eventQueue: ActorRef[EventQueue.Push] = _
  private var tradingPair: String = _

  def initialize(
      dh: BaseDataHandler,
      eq: ActorRef[EventQueue.Push],
      pair: String
  ): Unit = {
    dataHandler = dh
    eventQueue = eq
    tradingPair = pair
    val series = dh.series
    val closePriceIndicator = new ClosePriceIndicator(series)
    val shortEma = new EMAIndicator(closePriceIndicator, 7)
    val longEma = new EMAIndicator(closePriceIndicator, 25)
    val entryRule = new CrossedUpIndicatorRule(shortEma, longEma)
    val exitRule = new CrossedDownIndicatorRule(shortEma, longEma)
    strategy = new BaseStrategy("Cross", entryRule, exitRule)
  }

  def calculateSignals(): Unit = {
    val series = dataHandler.series
    val endIndex = series.getEndIndex()
    val lastBar = series.getBar(endIndex)
    if (strategy.shouldEnter(endIndex)) {
      val event = EventHandler.SignalEvent(
        tradingPair,
        lastBar.getClosePrice().doubleValue(),
        Constants.OrderSides.BUY
      )
      eventQueue ! EventQueue.Push(event)
    } else if (strategy.shouldExit(endIndex)) {
      val event = EventHandler.SignalEvent(
        tradingPair,
        lastBar.getClosePrice().doubleValue(),
        Constants.OrderSides.SELL
      )
      eventQueue ! EventQueue.Push(event)
    }
  }
}
