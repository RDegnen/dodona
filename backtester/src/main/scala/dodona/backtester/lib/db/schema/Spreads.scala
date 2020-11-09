package dodona.backtester.lib.db.schema

import slick.driver.SQLiteDriver.api._
import slick.lifted.Tag

case class Spread(id: Int, time: Long, bid: BigDecimal, ask: BigDecimal, symbol: String)

class Spreads(tag: Tag) extends Table[(Int, Long, BigDecimal, BigDecimal, String)](tag, "spreads") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def time = column[Long]("time")
  def bid = column[BigDecimal]("bid")
  def ask = column[BigDecimal]("ask")
  def symbol = column[String]("symbol")
  def * = (id, time, bid, ask, symbol)
}