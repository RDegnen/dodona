package dodona.backtester.lib.db.schema

import dodona.backtester.lib.domain.Order
import slick.jdbc.JdbcProfile

class OrdersDAO(val profile: JdbcProfile) {
  import profile.api._

  private type orderTuple = (Int, String, String, BigDecimal, BigDecimal, Long)

  class Orders(tag: Tag) extends Table[orderTuple](tag, "orders") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def symbol = column[String]("symbol")
    def side = column[String]("side")
    def quantity = column[BigDecimal]("quantity")
    def price = column[BigDecimal]("price")
    def time = column[Long]("time")
    def * = (id, symbol, side, quantity, price, time)
  }
  val orders = TableQuery[Orders]

  def create: DBIO[Unit] =
    orders.schema.create

  def insert(order: Order): DBIO[Int] =
    orders += (order.id, order.symbol, order.side, order.quantity, order.price, order.time)
}