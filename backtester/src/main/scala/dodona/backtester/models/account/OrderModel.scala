package dodona.backtester.models.account

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem, Scheduler}
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.util.Timeout
import dodona.backtester.actors.{MainSystem, Prices}
import dodona.backtester.lib.Pairs
import dodona.backtester.lib.config.AccountConfig
import dodona.backtester.lib.db.DB
import dodona.backtester.lib.db.schema.OrdersDAO
import dodona.backtester.lib.domain.Order
import dodona.backtester.services.account.WalletService
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.SQLiteProfile

object OrderModel {
  def apply()(implicit system: ActorSystem[MainSystem.Protocol]): OrderModel = {
    implicit val timeout: Timeout = 10.seconds
    implicit val ec = system.executionContext

    val prices = Await
      .result(
        system.ask(MainSystem.GetPricesActor(_)),
        10.seconds
      )
      .asInstanceOf[MainSystem.PricesActor]

    new OrderModel(prices.actor)
  }
}

class OrderModel(pricesRef: ActorRef[Prices.Protocol])(implicit
    scheduler: Scheduler,
    ec: ExecutionContext
) {
  private val tradingFee = AccountConfig.TRADING_FEE
  private val wallet = WalletService
  private val buy = "BUY"
  private val sell = "SELL"
  val dao = new OrdersDAO(SQLiteProfile)
  val db = new DB(Database.forConfig("DodonaBacktester.db"))

  def placeOrder(
      symbol: String,
      quantity: BigDecimal,
      side: String
  ): Future[HttpResponse] = {
    implicit val timeout: Timeout = 10.seconds
    val pair = Pairs.getPair(symbol)
    side match {
      case `buy`  => placeBuy(symbol, quantity, pair)
      case `sell` => placeSell(symbol, quantity, pair)
    }
  }

  private def placeBuy(
      symbol: String,
      quantity: BigDecimal,
      pair: (String, String)
  )(implicit
      timeout: Timeout
  ): Future[HttpResponse] = {
    pricesRef
      .ask(ref => Prices.GetPrice(symbol, ref))
      .map(priceValue => {
        val walletAmount = wallet.getBalance(pair._2)
        val marketPrice = priceValue.value
        val orderTotalValue = calculateOrderValue(
          marketPrice,
          quantity,
          (a: BigDecimal, b: BigDecimal) => a + b
        )
        if (walletAmount >= orderTotalValue) {
          val time = System.currentTimeMillis()
          executeOrder(symbol, buy, quantity, marketPrice, time)
          wallet.updateBalance(pair._1, quantity)
          wallet.updateBalance(pair._2, walletAmount - orderTotalValue)
          HttpResponse(StatusCodes.OK)
        } else {
          HttpResponse(
            StatusCodes.BadRequest,
            entity = s"Not enough ${pair._2} in your wallet"
          )
        }
      })
  }

  private def placeSell(
      symbol: String,
      quantity: BigDecimal,
      pair: (String, String)
  )(implicit
      timeout: Timeout
  ): Future[HttpResponse] = {
    pricesRef
      .ask(ref => Prices.GetPrice(symbol, ref))
      .map(priceValue => {
        val walletAmount = wallet.getBalance(pair._1)
        val marketPrice = priceValue.value
        val valueOfSell = calculateOrderValue(
          marketPrice,
          quantity,
          (a: BigDecimal, b: BigDecimal) => a - b
        )
        if (walletAmount >= quantity) {
          val time = System.currentTimeMillis()
          executeOrder(symbol, sell, quantity, marketPrice, time)
          wallet.updateBalance(pair._2, valueOfSell)
          wallet.updateBalance(pair._1, walletAmount - quantity)
          HttpResponse(StatusCodes.OK)
        } else {
          HttpResponse(
            StatusCodes.BadRequest,
            entity = s"Not enough ${pair._1} in your wallet"
          )
        }
      })
  }

  private def calculateOrderValue(
      marketPrice: BigDecimal,
      quantity: BigDecimal,
      fn: (BigDecimal, BigDecimal) => BigDecimal
  ): BigDecimal = {
    val quantityPrice = marketPrice * quantity
    val fee = quantityPrice * tradingFee
    fn(quantityPrice, fee)
  }

  private def executeOrder(
      symbol: String,
      side: String,
      quantity: BigDecimal,
      price: BigDecimal,
      time: Long
  ): Unit = {
    val order = Order(0, symbol, side, quantity, price, time)
    val action = dao.insert(order)
    db.run(action)
  }
}
