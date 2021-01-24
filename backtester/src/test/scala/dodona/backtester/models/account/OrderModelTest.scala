package dodona.backtester.models.account

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.BeforeAndAfterAll
import akka.actor.typed.scaladsl.AskPattern._
import dodona.backtester.actors.Prices
import dodona.backtester.lib.db.schema.OrdersDAO
import slick.jdbc.H2Profile
import dodona.backtester.lib.db.DB
import dodona.backtester.lib.config.DatabaseConfig
import scala.util.Success
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes
import scala.util.Failure
import akka.actor.testkit.typed.scaladsl.ActorTestKit
import dodona.backtester.actors.Wallet
import org.scalatest.concurrent.Waiters.Waiter
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.Span
import org.scalatest.concurrent.Waiters.Dismissals
import org.scalatest.time.Seconds
import org.scalatest.BeforeAndAfterEach

class OrderModelTest extends AnyFunSpec with BeforeAndAfterAll with BeforeAndAfterEach {
  val testKit = ActorTestKit()
  implicit val scheduler = testKit.scheduler
  implicit val ec = testKit.system.executionContext

  val symbol = "ETHUSD"
  val w = new Waiter
  val patienceConfigTimeout = PatienceConfiguration.Timeout(Span(2, Seconds))
  val patienceConfigDismissals = Dismissals(1)
  val pricesRef = testKit.spawn(Prices(), "prices")
  val walletRef = testKit.spawn(Wallet(), "wallet")
  val walletProbe = testKit.createTestProbe[Wallet.AssetValue]()
  val ordersDao = new OrdersDAO(H2Profile)
  val database = new DB(DatabaseConfig.h2)
  val testOrderModel = new OrderModel(pricesRef, walletRef) {
    override val dao: OrdersDAO = ordersDao
    override val db: DB = database
  }
  
  override protected def beforeAll(): Unit = {
    ordersDao.create
    pricesRef ! Prices.AdjustPrice(symbol, 100)
  }

  override protected def afterAll(): Unit = {
    testKit.stop(pricesRef)
    testKit.stop(walletRef)
    testKit.shutdownTestKit()
    database.close
  }

  override protected def afterEach(): Unit = {
    w.await(patienceConfigTimeout, patienceConfigDismissals)
  }

  describe("OrderModel") {
    it("should place a buy order") {
      val quantity = 2
      val order = testOrderModel.placeOrder(symbol, quantity, "BUY")
      order.onComplete {
        case Success(value) => 
          walletRef ! Wallet.GetAssetBalance("ETH", walletProbe.ref)
          walletProbe.expectMessage(Wallet.AssetValue(quantity))
          assert(value === HttpResponse(StatusCodes.OK))
          w.dismiss()
        case Failure(exception) => println(exception)
      }
    }

    it("should place a sell order") {
      pricesRef ! Prices.AdjustPrice(symbol, 200)

      val order = testOrderModel.placeOrder(symbol, 2, "SELL")
      order.onComplete {
        case Success(value) => 
          walletRef ! Wallet.GetAssetBalance("USD", walletProbe.ref)
          walletProbe.expectMessage(Wallet.AssetValue(1199.400))
          assert(value === HttpResponse(StatusCodes.OK))
          w.dismiss()
        case Failure(exception) => println(exception)
      }
    }
  }
}
