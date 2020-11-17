package dodona.backtester.models

import scala.concurrent.{ExecutionContext, Future}

import dodona.backtester.lib.db.DB
import dodona.backtester.lib.db.schema.Candlesticks
import slick.driver.SQLiteDriver.api._
import slick.lifted.TableQuery

class CandlestickModel() {
  private val db = DB.db

  def getOHLCbySymbol(symbol: String)(implicit ec: ExecutionContext): Future[Seq[CandlestickReturnType]] = {
    val candlesticks = TableQuery[Candlesticks]
    val query = candlesticks.filter(_.symbol === symbol).result

    db.run(query).map(candlesticks => {
      candlesticks.map(cs => {
        BinanceCandlestick(cs.openTime, cs.open, cs.high, cs.low, cs.close, cs.volume, cs.closeTime)
      })
    })
  }
}
