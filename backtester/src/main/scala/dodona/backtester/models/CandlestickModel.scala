package dodona.backtester.models

import dodona.backtester.lib.db.DB
import slick.lifted.TableQuery
import dodona.backtester.lib.db.schema.Candlesticks
import slick.driver.SQLiteDriver.api._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import dodona.constants.Exchanges

class CandlestickModel(exchange: String) {
  private val db = DB.db

  def getCandlesticks()(implicit ec: ExecutionContext): Future[Seq[CandlestickReturnType]] = {
    val candlesticks = TableQuery[Candlesticks]
    val query = candlesticks.result

    db.run(query).map(candlesticks => {
      candlesticks.map(cs => {
        exchange match {
          case Exchanges.BINANCE =>
            BinanceCandlestick(cs.openTime, cs.open, cs.high, cs.low, cs.close, cs.volume, cs.closeTime)
          case Exchanges.KRAKEN => KrakenCandlestick()
        }
      })
    })
  }
}
