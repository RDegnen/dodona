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
import dodona.lib.domain.dodona.market.ExchangeInfo
import dodona.lib.domain.dodona.market.LotSize
import dodona.lib.domain.dodona.market.Filter
import dodona.lib.domain.dodona.market.Symbol

class BacktesterPortfolio(quoteAsset: String)(implicit
    val system: ActorSystem[MainSystem.Protocol],
    ec: ExecutionContext
) extends BasePortfolio(quoteAsset) {
  protected val httpClient: BaseHttpClient = new BacktesterHttpClient()
  private var dataHandler: BaseDataHandler = _
  private var eventQueue: ActorRef[EventQueue.Push] = _
  private var holdings: Map[String, BigDecimal] = Map()
  private var positions: Map[String, Position] = Map()
  private var symbols: List[Symbol] = _

  def initialize(dh: BaseDataHandler, eq: ActorRef[EventQueue.Push]): Unit = {
    dataHandler = dh
    eventQueue = eq
    constructHoldings
    getExchangeInfo
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
    val pair = s"$base$quote"
    val lotSize = getFilters(pair)
      .flatMap(filters => {
        filters
          .find {
            case LotSize(_, _, _, _) => true
            case _                   => false
          }
          .asInstanceOf[Option[LotSize]]
      })

    for {
      fundsToRisk <- holdings.get(quote).map(value => value * 0.25)
      ls <- lotSize
      quantity <- Some(
        ((fundsToRisk / price) / ls.stepSize)
          .setScale(0, BigDecimal.RoundingMode.HALF_UP) * ls.stepSize
      )
    } yield EventHandler.OrderEvent(pair, "MARKET", quantity, side)
  }

  private def generateSellOrder(
      base: String,
      quote: String,
      price: BigDecimal,
      side: String
  ): Option[EventHandler.OrderEvent] = {
    for {
      baseAmount <- holdings.get(base)
    } yield EventHandler.OrderEvent(s"$base$quote", "MARKET", baseAmount, side)
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

  private def getExchangeInfo(): Unit = {
    httpClient
      .generateRequest[ExchangeInfo](
        PUBLIC,
        HttpMethods.GET,
        "/market/exchangeInfo"
      )
      .onComplete {
        case Success(value) =>
          symbols = value.symbols
        case Failure(exception) => println(exception)
      }
  }

  private def getFilters(pair: String): Option[List[Filter]] =
    symbols
      .find(symbol => symbol.symbol == pair)
      .map(symbol => symbol.filters)
}
