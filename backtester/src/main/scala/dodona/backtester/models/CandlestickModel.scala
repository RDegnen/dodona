package dodona.backtester.models

import scala.concurrent.{ExecutionContext, Future}

import dodona.backtester.lib.db.DB
import dodona.backtester.lib.db.schema.Candlesticks
import dodona.constants.Exchanges
import slick.driver.SQLiteDriver.api._
import slick.lifted.TableQuery

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
