package dodona.backtester.models

import dodona.backtester.lib.db.DB
import slick.lifted.TableQuery
import dodona.backtester.lib.db.schema.Candlesticks
import dodona.backtester.lib.db.schema.Candlestick
import slick.driver.SQLiteDriver.api._
import scala.concurrent.ExecutionContext

class CandlestickModel {
  private val db = DB.db

  def getCandlesticks()(implicit ec: ExecutionContext): Unit = {
    val candlesticks = TableQuery[Candlesticks]
    val query = candlesticks.result

    db.run(query).foreach(println)

    // db.stream(query).foreach(println)

    // db.run(query).map(_.foreach {
    //   case Candlestick(id,symbol,interval,open,high,low,close,volume,openTime,closeTime) =>
    //     println(close)
    // })
  }
}
