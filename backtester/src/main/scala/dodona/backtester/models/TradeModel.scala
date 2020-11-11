package dodona.backtester.models

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

import akka.NotUsed
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.scaladsl.{Flow, Sink, Source}
import dodona.backtester.lib.db.DB
import dodona.backtester.lib.db.schema.Trades
import io.circe.syntax._
import slick.driver.SQLiteDriver.api._
import slick.lifted.TableQuery

class TradeModel {
  private val db = DB.db

  def streamTrades()(implicit ec: ExecutionContext): Future[Flow[Message, Message, NotUsed]] = {
    val trades = TableQuery[Trades]
    val query = trades.result

    db.run(query).map(trades => {
      val source = Source(trades)
        .throttle(1, 1.millisecond)
        .map(m => TextMessage(m.asJson.toString()))

      Flow.fromSinkAndSource(Sink.ignore, source)
    })
  }
}