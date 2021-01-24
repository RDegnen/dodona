package dodona.portfolio.portfolios

import akka.actor.typed.ActorRef
import dodona.events.EventQueue
import dodona.data.BaseDataHandler
import dodona.events.EventHandler
import dodona.lib.http.{BaseHttpClient, PUBLIC}
import dodona.lib.http.clients.BacktesterHttpClient
import akka.http.scaladsl.model.HttpMethods
import akka.actor.typed.ActorSystem
import dodona.MainSystem
import scala.concurrent.ExecutionContext
import scala.util.Success
import scala.util.Failure
import dodona.portfolio.Position
import dodona.portfolio.BasePortfolio

class BacktesterPortfolio(quoteAsset: String)(implicit
    val system: ActorSystem[MainSystem.Protocol],
    ec: ExecutionContext
) extends BasePortfolio(quoteAsset) {
  protected val httpClient: BaseHttpClient = new BacktesterHttpClient()
  private var dataHandler: BaseDataHandler = _
  private var eventQueue: ActorRef[EventQueue.Push] = _
  // Held assets
  private var holdings: Map[String, BigDecimal] = Map()
  // Open and previous orders
  private var positions: Map[String, Position] = Map()

  def initialize(dh: BaseDataHandler, eq: ActorRef[EventQueue.Push]): Unit = {
    dataHandler = dh
    eventQueue = eq
    constructHoldings
  }

  def updateSignal(pair: String, price: BigDecimal, side: String): Unit = {
    splitPair(pair) match {
      case Some((base, quote)) =>
        val orderSignal = generateOrder(base, quote, price, side)
        eventQueue ! EventQueue.Push(orderSignal)
      case None => println("Pair not found")
    }
  }

  def updateFill(
      pair: String,
      action: String,
      status: String,
      price: BigDecimal,
      quantity: BigDecimal,
      transactionTime: Long
  ): Unit = {
    constructHoldings
    updatePosition(pair, action, status, price, quantity, transactionTime)
  }

  private def generateOrder(
      base: String,
      quote: String,
      price: BigDecimal,
      side: String
  ): EventHandler.OrderEvent = {
    // Incase I forget I am currently working on generating orders.
    // This is a super simple implamentation right now. Need to use 
    // price filters for actual impl https://binance-docs.github.io/apidocs/spot/en/#filters
    val quoteAmount: BigDecimal = holdings.getOrElse(quote, 0)
    val fundsToRiskPercent = 0.25
    val fundsToRisk = quoteAmount * fundsToRiskPercent
    val quantity = fundsToRisk / price
    val pair = s"$base$quote"
    EventHandler.OrderEvent(pair, "MARKET", quantity, side)
  }

  private def constructHoldings(): Unit = {
    httpClient
      .generateRequest[Map[String, BigDecimal]](
        PUBLIC,
        HttpMethods.GET,
        "/account/balance"
      )
      .onComplete {
        case Success(value) =>
          holdings = value
        case Failure(exception) =>
          println(exception)
      }
  }

  private def updatePosition(
      pair: String,
      action: String,
      status: String,
      price: BigDecimal,
      quantity: BigDecimal,
      transactionTime: Long
  ): Unit = {
    val position = Position(action, status, price, quantity, transactionTime)
    positions += (pair -> position)
  }
}
