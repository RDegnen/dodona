package dodona.http

import org.scalatest.funspec.AnyFunSpec
import dodona.constants.Exchanges
import dodona.constants.RequestTypes
import akka.http.scaladsl.model.HttpMethods
import io.circe.Decoder
import io.circe.generic.semiauto._
import akka.actor.ActorSystem
import scala.util.Success
import scala.util.Failure
import dodona.http.mappers.DodonaEnpoints

case class Signature(signature: String)

class HttpClientTest extends AnyFunSpec {
  describe("HttpClient") {
    implicit val system = ActorSystem()
    implicit val executionContext = system.dispatcher
    implicit val SignatureDecoder: Decoder[Signature] = deriveDecoder
    val nonceFn = () => 1601321566354L
  
    it("should sign a Kraken request") {
      val krakenClient = new TestHttpClient(Exchanges.KRAKEN)
      val expectedSignature = Signature("lMsyNQQ4mfWadKpSpz6UDy8OHv7INrRsWuP+/NffDimfumdOE/OhzVdUTiaZw4S4yfFvCRIL2Lr7BxURZ/pvEA==")
      val response = krakenClient.request[Signature](
        RequestTypes.SIGNED,
        HttpMethods.GET,
        DodonaEnpoints.CANDLESTICKS,
        nonceGenerator = nonceFn
      )

      response.onComplete {
        case Success(value) => assert(value == expectedSignature)
        case Failure(exception) => 
      }
    }

    it("should sign a Binance request") {
      val binanceClient = new TestHttpClient(Exchanges.BINANCE)
      val expectedSignature = Signature("e4d5a7fe32904551586a5161dbad46238e7b8533834850850869139819b489ec")
      val response = binanceClient.request[Signature](
        RequestTypes.SIGNED,
        HttpMethods.GET,
        DodonaEnpoints.CANDLESTICKS,
        nonceGenerator = nonceFn
      )

      response.onComplete {
        case Success(value) => assert(value == expectedSignature)
        case Failure(exception) => 
      }
    }
  }
}