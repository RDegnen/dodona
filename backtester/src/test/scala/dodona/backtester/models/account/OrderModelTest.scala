package dodona.backtester.models.account

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.BeforeAndAfterAll
import akka.actor.typed.ActorSystem
import dodona.backtester.actors.MainSystem
import scala.concurrent.Await
import akka.util.Timeout
import scala.concurrent.duration._
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

class OrderModelTest extends AnyFunSpec with BeforeAndAfterAll {
  // FIXME maybe use the testing actors
  implicit val timeout: Timeout = 10.seconds
  implicit val system = ActorSystem(MainSystem(), "test")
  implicit val ec = system.executionContext

  val symbol = "ETHUSD"
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
  val ordersDao = new OrdersDAO(H2Profile)
  val database = new DB(DatabaseConfig.h2)
  val testOrderModel = new OrderModel(prices.actor, wallet.actor) {
    override val dao: OrdersDAO = ordersDao
    override val db: DB = database
  }

  override protected def beforeAll(): Unit = {
    ordersDao.create
    prices.actor ! Prices.AdjustPrice(symbol, 100)
  }

  override protected def afterAll(): Unit = {
    system.terminate()
    database.close
  }

  describe("OrderModel") {
    it("should place a buy order") {
      testOrderModel.placeOrder(symbol, 2, "BUY").onComplete {
        case Success(value) => {
          assert(value == HttpResponse(StatusCodes.OK))
        }
        case Failure(exception) => 
      }
    }

    it("should place a sell order") {
      // FIXME fix this test
      prices.actor ! Prices.AdjustPrice(symbol, 200)

      testOrderModel.placeOrder(symbol, 2, "SELL").onComplete {
        case Success(value) => {
          assert(value == HttpResponse(
            StatusCodes.BadRequest,
            entity = "Not enough ETH in your wallet"
          ))
        }
        case Failure(exception) => println(exception)
      }
    }
  }
}