package dodona.backtester.models.account

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem, Scheduler}
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.util.Timeout
import dodona.backtester.actors.{MainSystem, Prices, Wallet}
import dodona.backtester.lib.Pairs
import dodona.backtester.lib.config.{AccountConfig, DatabaseConfig}
import dodona.backtester.lib.db.DB
import dodona.backtester.lib.db.schema.OrdersDAO
import dodona.backtester.lib.domain.Order
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

    val wallet = Await
      .result(
        system.ask(MainSystem.GetWalletActor(_)),
        10.seconds
      )
      .asInstanceOf[MainSystem.WalletActor]

    new OrderModel(prices.actor, wallet.actor)
  }
}

class OrderModel(
    pricesRef: ActorRef[Prices.Protocol],
    walletRef: ActorRef[Wallet.Protocol]
)(implicit
    scheduler: Scheduler,
    ec: ExecutionContext
) {
  private val tradingFee = AccountConfig.TRADING_FEE
  private val buy = "BUY"
  private val sell = "SELL"
  val dao = new OrdersDAO(SQLiteProfile)
  val db = new DB(DatabaseConfig.sqlite)

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
    val marketValue = getMarketValue(symbol)
    val orderTotalValue = marketValue
      .map(marketPrice => {
        calculateOrderValue(
          marketPrice,
          quantity,
          (a: BigDecimal, b: BigDecimal) => a + b
        )
      })

    val buyOrError = (
        marketPrice: BigDecimal,
        orderTotalValue: BigDecimal,
        fiatBalance: BigDecimal
    ) => {
      if (fiatBalance >= orderTotalValue) {
        val time = System.currentTimeMillis()
        executeOrder(symbol, buy, quantity, marketPrice, time)
        walletRef ! Wallet.UpdateBalance(pair._1, quantity)
        walletRef ! Wallet.UpdateBalance(
          pair._2,
          fiatBalance - orderTotalValue
        )
        HttpResponse(StatusCodes.OK)
      } else {
        HttpResponse(
          StatusCodes.BadRequest,
          entity = s"Not enough ${pair._2} in your wallet"
        )
      }
    }

    for {
      mk <- marketValue
      otv <- orderTotalValue
      b <- getBalance(pair._2)
    } yield buyOrError(mk, otv, b)
  }

  private def placeSell(
      symbol: String,
      quantity: BigDecimal,
      pair: (String, String)
  )(implicit
      timeout: Timeout
  ): Future[HttpResponse] = {
    val marketValue = getMarketValue(symbol)
    val valueOfSell = marketValue
      .map(marketPrice => {
        calculateOrderValue(
          marketPrice,
          quantity,
          (a: BigDecimal, b: BigDecimal) => a - b
        )
      })

    val sellOrError = (
        marketPrice: BigDecimal,
        valueOfSell: BigDecimal,
        coinBalance: BigDecimal,
        fiatBalance: BigDecimal
    ) => {
      if (coinBalance >= quantity) {
        val time = System.currentTimeMillis()
        executeOrder(symbol, sell, quantity, marketPrice, time)
        walletRef ! Wallet.UpdateBalance(pair._2, fiatBalance + valueOfSell)
        walletRef ! Wallet.UpdateBalance(pair._1, coinBalance - quantity)
        HttpResponse(StatusCodes.OK)
      } else {
        HttpResponse(
          StatusCodes.BadRequest,
          entity = s"Not enough ${pair._1} in your wallet"
        )
      }
    }

    for {
      mk <- marketValue
      vos <- valueOfSell
      b1 <- getBalance(pair._1)
      b2 <- getBalance(pair._2)
    } yield sellOrError(mk, vos, b1, b2)
  }

  private def getMarketValue(
      symbol: String
  )(implicit timeout: Timeout): Future[BigDecimal] =
    pricesRef
      .ask(ref => Prices.GetPrice(symbol, ref))
      .map(_.value)

  private def getBalance(
      symbol: String
  )(implicit timeout: Timeout): Future[BigDecimal] =
    walletRef
      .ask(ref => Wallet.GetBalance(symbol, ref))
      .map(_.value)

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
