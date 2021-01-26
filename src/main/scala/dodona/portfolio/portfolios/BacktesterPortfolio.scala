package dodona.portfolio.portfolios

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.HttpMethods
import dodona.MainSystem
import dodona.data.BaseDataHandler
import dodona.events.{EventHandler, EventQueue}
import dodona.lib.http.clients.BacktesterHttpClient
import dodona.lib.http.{BaseHttpClient, PUBLIC}
import dodona.portfolio.{BasePortfolio, Position}
import dodona.Constants

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
        if (side == Constants.OrderSides.BUY) {
          val orderSignal = generateBuyOrder(base, quote, price, side)
          orderSignal.foreach(order => {
            eventQueue ! EventQueue.Push(order)
          })
        } else if (side == Constants.OrderSides.SELL) {
          val orderSignal = generateSellOrder(base, quote, price, side)
          orderSignal.foreach(order => {
            eventQueue ! EventQueue.Push(order)
          })
        }
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
    println(holdings)
  }

  private def generateBuyOrder(
      base: String,
      quote: String,
      price: BigDecimal,
      side: String
  ): Option[EventHandler.OrderEvent] = {
    // Incase I forget I am currently working on generating orders.
    // This is a super simple implamentation right now. Need to use 
    // price filters for actual impl https://binance-docs.github.io/apidocs/spot/en/#filters
    val quoteAmount: BigDecimal = holdings.getOrElse(quote, 0)
    if (quoteAmount > 0) {
      val fundsToRiskPercent = 0.25
      val fundsToRisk = quoteAmount * fundsToRiskPercent
      val quantity = fundsToRisk / price
      val pair = s"$base$quote"
      Some(EventHandler.OrderEvent(pair, "MARKET", quantity, side))
    } else {
      None
    }
  }

  private def generateSellOrder(
      base: String,
      quote: String,
      price: BigDecimal,
      side: String
  ): Option[EventHandler.OrderEvent] = {
    val baseAmount: BigDecimal = holdings.getOrElse(base, 0)
    if (baseAmount > 0) {
      val pair = s"$base$quote"
      Some(EventHandler.OrderEvent(pair, "MARKET", baseAmount, side))
    } else {
      None
    }
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
