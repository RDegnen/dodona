package dodona.data

import java.time._
import java.time.temporal.ChronoUnit
import java.util.Date

import dodona.lib.domain.dodona.market.{Candlestick, Trade}

class CandlestickBuilder(interval: Int) {
  private var values: Seq[Trade] = Seq.empty

  def addTrade(trade: Trade): Option[Candlestick] = {
    values = values :+ trade
    val ldt1 = getLocalDateTime(values.head.time)
    val ldt2 = getLocalDateTime(values.last.time)
    val diff = getTimeDifference(ldt1, ldt2)
    if (diff >= interval) {
      val prices = values.map(_.price)
      val candlestick = Candlestick(
        values.head.time,
        values.last.time,
        prices.head,
        prices.max,
        prices.min,
        prices.length,
        prices.last
      )
      values = values.drop(values.length)
      Some(candlestick)
    } else {
      None
    }
  }

  private def getLocalDateTime(long: Long): LocalDateTime = {
    val date = new Date(long)
    LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
  }

  private def getTimeDifference(
      ldt1: LocalDateTime,
      ldt2: LocalDateTime
  ): Long =
    ldt1.until(ldt2, ChronoUnit.MINUTES)
}
