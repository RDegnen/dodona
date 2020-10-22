package dodona.backtester.lib.db

import slick.driver.SQLiteDriver.api._

object DB {
  final val db = Database.forConfig("db")
}
