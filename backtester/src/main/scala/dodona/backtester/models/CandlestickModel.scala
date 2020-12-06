package dodona.backtester.models

import scala.concurrent.{ExecutionContext, Future}

import dodona.backtester.lib.db.DB
import dodona.backtester.lib.db.schema.CandlesticksDAO
import dodona.backtester.lib.domain.Candlestick
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.SQLiteProfile

class CandlestickModel() {
  val dao = new CandlesticksDAO(SQLiteProfile)
  val db = new DB(Database.forConfig("DodonaBacktester.db"))
  import dao.profile.api._

  def getOHLCbySymbol(symbol: String)(implicit ec: ExecutionContext): Future[Seq[Candlestick]] = {
    val query = dao.candlesticks.filter(_.symbol === symbol).result

    db.run(query).map(candlesticks => {
      candlesticks.map(cs => {
        Candlestick(cs.openTime, cs.open, cs.high, cs.low, cs.close, cs.volume, cs.closeTime)
      })
    })
  }
}
