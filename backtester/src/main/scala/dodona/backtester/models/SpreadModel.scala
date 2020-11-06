package dodona.backtester.models

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

import akka.NotUsed
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.scaladsl.{Flow, Sink, Source}
import dodona.backtester.lib.db.DB
import dodona.backtester.lib.db.schema.Spreads
import io.circe.syntax._
import slick.driver.SQLiteDriver.api._
import slick.lifted.TableQuery

class SpreadModel {
  private val db = DB.db

  def streamSpreads()(implicit ec: ExecutionContext): Future[Flow[Message, Message, NotUsed]] = {
    val spreads = TableQuery[Spreads]
    val query = spreads.result

    db.run(query).map(spreads => {
      val source = Source(spreads)
        .throttle(1, 1.second)
        .map(m => TextMessage(m.asJson.toString()))
      val sink = Sink.ignore

      Flow.fromSinkAndSource(sink, source)
    })
  }
}