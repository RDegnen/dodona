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
import dodona.backtester.lib.db.IDB
import scala.concurrent.Future
import slick.basic.DatabasePublisher
import slick.driver.SQLiteDriver.api._

class OrderModelTest extends AnyFunSpec with BeforeAndAfterAll {
  implicit val timeout: Timeout = 10.seconds
  implicit val system = ActorSystem(MainSystem(), "test")
  implicit val ec = system.executionContext
  val prices = Await
    .result(
        system.ask(MainSystem.GetPricesActor(_)),
        10.seconds
      )
    .asInstanceOf[MainSystem.PricesActor]

  override protected def afterAll(): Unit = system.terminate()

  prices.actor ! Prices.AdjustPrice("ETHUSD", 100)

  describe("OrderModel") {
    val testOrderModel = new OrderModel(prices.actor) {}
  }
}