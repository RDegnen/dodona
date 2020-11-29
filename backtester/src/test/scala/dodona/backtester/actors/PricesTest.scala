package dodona.backtester.actors

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.BeforeAndAfterAll
import akka.actor.testkit.typed.scaladsl.ActorTestKit

class PricesTest extends AnyFunSpec with BeforeAndAfterAll {
  val testKit = ActorTestKit()

  override protected def afterAll(): Unit = testKit.shutdownTestKit()

  describe("Prices actor") {
    it("should adjust prices for a symbol") {
      val prices = testKit.spawn(Prices(), "prices")
      val probe = testKit.createTestProbe[Prices.PriceValue]()

      prices ! Prices.AdjustPrice("ETHUSD", 100.02)
      prices ! Prices.AdjustPrice("ETHUSD", 120.10)
      prices ! Prices.GetPrice("ETHUSD", probe.ref)
      
      probe.expectMessage(Prices.PriceValue(120.10))

      testKit.stop(prices)
    }
  }
}