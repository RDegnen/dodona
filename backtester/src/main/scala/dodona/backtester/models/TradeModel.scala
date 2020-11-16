package dodona.backtester.models

import scala.concurrent.duration._

import akka.NotUsed
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.scaladsl.{Flow, Sink, Source}
import dodona.backtester.lib.db.DB
import dodona.backtester.lib.db.schema.Trades
import io.circe.syntax._
import slick.driver.SQLiteDriver.api._
import slick.lifted.TableQuery
import dodona.backtester.lib.domain.Trade
import slick.basic.DatabasePublisher
import scala.concurrent.ExecutionContext

class TradeModel {
  private val db = DB.db

  def streamTradesBySymbol(
      symbol: String
  )(implicit ec: ExecutionContext): Flow[Message, Message, NotUsed] = {
    val trades = TableQuery[Trades]
    val query = trades.filter(_.symbol === symbol).result
    val p: DatabasePublisher[
      (Int, BigDecimal, BigDecimal, BigDecimal, Long, String)
    ] = db.stream(query.transactionally.withStatementParameters(fetchSize = 5000))

    val source = Source.fromPublisher(p)
      .throttle(1, 1.millisecond)
      .map(m => Trade(m._1, m._2, m._3, m._4, m._5, m._6))
      .map(m => TextMessage(m.asJson.toString()))

    Flow.fromSinkAndSource(Sink.ignore, source)
  }
}
