package dodona.strategy.strategies

import akka.actor.typed.ActorSystem
import scala.concurrent.ExecutionContext
import dodona.strategy.IStrategy
import dodona.MainSystem
import dodona.data.BaseDataHandler
import akka.actor.typed.ActorRef
import dodona.events.EventQueue
import org.ta4j.core.indicators.adx.ADXIndicator
import org.ta4j.core.indicators.RSIIndicator
import org.ta4j.core.indicators.helpers.ClosePriceIndicator
import dodona.events.EventHandler
import dodona.Constants

class ThreeAmigos(implicit
    val system: ActorSystem[MainSystem.Protocol],
    ec: ExecutionContext
) extends IStrategy {
  private var dataHandler: BaseDataHandler = _
  private var eventQueue: ActorRef[EventQueue.Push] = _
  private var tradingPair: String = _
  private var adx: ADXIndicator = _
  private var rsi: RSIIndicator = _
  private var tradeOpen: Boolean = false

  def initialize(dh: BaseDataHandler, eq: ActorRef[EventQueue.Push], pair: String): Unit = {
    dataHandler = dh
    eventQueue = eq
    tradingPair = pair
    val series = dh.series
    val close = new ClosePriceIndicator(series)
    adx = new ADXIndicator(series, 14)
    rsi = new RSIIndicator(close, 14)
  }

  def calculateSignals(): Unit = {
    val series = dataHandler.series
    val endIndex = series.getEndIndex()
    val lastBar = series.getBar(endIndex)
    val lookBackBig = series.getBar(endIndex - 20)
    val lookBackSmall = series.getBar(endIndex - 10)
    val adxValue = adx.getValue(endIndex).doubleValue()
    val rsiValue = rsi.getValue(endIndex).doubleValue()
    val lastBarClose = lastBar.getClosePrice().doubleValue()
    val lookBackBigClose = lookBackBig.getClosePrice().doubleValue()
    val lookBackSmallClose = lookBackSmall.getClosePrice().doubleValue()

    if (adxValue > 25) {
      if (rsiValue < 50 && lastBarClose < lookBackBigClose) {
        if (lastBarClose > lookBackSmallClose && !tradeOpen) {
          val event = EventHandler.SignalEvent(
            tradingPair,
            lastBarClose,
            Constants.OrderSides.BUY
          )
          eventQueue ! EventQueue.Push(event)
          tradeOpen = true
        }
      } else if (rsiValue > 50 && lastBarClose > lookBackBigClose) {
        if (lastBarClose < lookBackSmallClose && tradeOpen) {
          val event = EventHandler.SignalEvent(
            tradingPair,
            lastBarClose,
            Constants.OrderSides.SELL
          )
          eventQueue ! EventQueue.Push(event)
          tradeOpen = false
        }
      }
    }
  }
}
