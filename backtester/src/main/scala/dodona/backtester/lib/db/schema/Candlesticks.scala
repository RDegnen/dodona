package dodona.backtester.lib.db.schema

import slick.lifted.Tag
import slick.driver.SQLiteDriver.api._

class Candlesticks(tag: Tag) extends Table[Candlestick](tag, "candlesticks") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def symbol = column[String]("symbol")
  def interval = column[String]("interval")
  def open = column[BigDecimal]("open")
  def high = column[BigDecimal]("high")
  def low = column[BigDecimal]("low")
  def close = column[BigDecimal]("close")
  def volume = column[BigDecimal]("volume")
  def openTime = column[Long]("open_time")
  def closeTime = column[Long]("close_time")
  def * = 
    (id, symbol, interval, open, high, low, close, volume, openTime, closeTime).mapTo[Candlestick]
}