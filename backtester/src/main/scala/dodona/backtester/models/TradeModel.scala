package dodona.backtester.models

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}

import akka.NotUsed
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.FlowShape
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Sink, Source}
import akka.util.Timeout
import dodona.backtester.actors.{MainSystem, Prices}
import dodona.backtester.lib.config.DatabaseConfig
import dodona.backtester.lib.db.DB
import dodona.backtester.lib.db.schema.TradesDAO
import dodona.backtester.lib.domain.Trade
import io.circe.syntax._
import org.reactivestreams.Publisher
import slick.basic.DatabasePublisher
import slick.jdbc.SQLiteProfile

object TradeModel {
  import akka.actor.typed.scaladsl.AskPattern._

  def apply()(implicit system: ActorSystem[MainSystem.Protocol]): TradeModel = {
    implicit val timeout: Timeout = 10.seconds

    val prices = Await.result(
      system.ask(MainSystem.GetPricesActor(_)),
      10.seconds
    ).asInstanceOf[MainSystem.PricesActor]

    new TradeModel(prices.actor)
  }
}

class TradeModel(pricesRef: ActorRef[Prices.Protocol]) {
  val dao = new TradesDAO(SQLiteProfile)
  val db = new DB(DatabaseConfig.sqlite)
  import dao.profile.api._

  private type tupleType =
    (Int, BigDecimal, BigDecimal, BigDecimal, Long, String)

  def streamTradesBySymbol(
      symbol: String
  )(implicit ec: ExecutionContext): Flow[Message, Message, NotUsed] = {
    val query = dao.trades.filter(_.symbol === symbol).result
    val p: DatabasePublisher[tupleType] =
      db.stream(query.transactionally.withStatementParameters(fetchSize = 5000))

    flowFromGraph(p, symbol)
  }

  def streamTradesBySymbolAndTime(symbol: String, timeToBegin: Long)(
    implicit ec: ExecutionContext
  ): Flow[Message, Message, NotUsed] = {
    val query =
      dao.trades.filter(_.symbol === symbol).filter(_.time > timeToBegin).result
    val p: DatabasePublisher[tupleType] =
      db.stream(query.transactionally.withStatementParameters(fetchSize = 5000))

    flowFromGraph(p, symbol)
  }

  private def flowFromGraph(
      p: Publisher[tupleType],
      symbol: String
  ): Flow[Message, Message, NotUsed] =
    Flow.fromGraph(GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._
        
      val sink = builder.add(Sink.ignore)
      val mapMsgToInt = builder.add(Flow[Message].map(msg => -1))

      val bcast = builder.add(Broadcast[tupleType](2))

      val source = builder.add(Source.fromPublisher(p))
      val mapSourceToMsg = builder.add(
        Flow[tupleType]
          // .throttle(1, 0.2.second)
          .throttle(1, 2.millisecond)
          .map(m => Trade(m._1, m._2, m._3, m._4, m._5, m._6))
          .map(m => TextMessage(m.asJson.toString()))
      )
      val adjustPriceFlow = builder.add(
        Flow[tupleType]
          .map(m => Trade(m._1, m._2, m._3, m._4, m._5, m._6))
          .to(Sink.foreach(trade => {
            pricesRef ! Prices.AdjustPrice(symbol, trade.price)
          }))
      )

      // Ignore incoming message
      mapMsgToInt ~> sink
      // Return trades from db
      source ~> bcast
      bcast.out(0) ~> mapSourceToMsg
      bcast.out(1) ~> adjustPriceFlow
      
      FlowShape(mapMsgToInt.in, mapSourceToMsg.out)
    })
}
