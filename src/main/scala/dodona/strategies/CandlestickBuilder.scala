package dodona.strategies

import dodona.lib.domain.dodona.market.Trade
import java.time._
import java.util.Date
import java.time.temporal.ChronoUnit

class CandlestickBuilder(interval: Int) {
  private var values: Seq[Trade] = Seq.empty

  def build(trade: Trade): Unit = {
    values = values :+ trade
    val ldt1 = getLocalDateTime(values.head.time)
    val ldt2 = getLocalDateTime(values.last.time)
    println(getTimeDifference(ldt1, ldt2))
  }

  private def getLocalDateTime(long: Long): LocalDateTime = {
    val date = new Date(long)
    LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
  }

  private def getTimeDifference(ldt1: LocalDateTime, ldt2: LocalDateTime): Long =
    ldt1.until(ldt2, ChronoUnit.MINUTES)
}
