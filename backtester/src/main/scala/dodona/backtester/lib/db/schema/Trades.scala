package dodona.backtester.lib.db.schema

import slick.driver.SQLiteDriver.api._
import slick.lifted.Tag

class Trades(tag: Tag) extends Table[(Int, BigDecimal, BigDecimal, BigDecimal, Long, String)](tag, "trades") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def price = column[BigDecimal]("price")
  def quantity = column[BigDecimal]("quantity")
  def quoteQuantity = column[BigDecimal]("quote_quantity")
  def time = column[Long]("time")
  def symbol = column[String]("symbol")
  def * = (id, price, quantity, quoteQuantity, time, symbol)
}