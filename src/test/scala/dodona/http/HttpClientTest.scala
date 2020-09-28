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

case class Signature(signature: String)

class HttpClientTest extends AnyFunSpec {
  describe("HttpClient") {
    implicit val system = ActorSystem()
    implicit val executionContext = system.dispatcher
    implicit val SignatureDecoder: Decoder[Signature] = deriveDecoder
    val nonceFn = () => 1601321566354L
    val client = new TestHttpClient()
  
    it("should sign a Kraken request") {
      val expectedSignature = Signature("U8OpTHxX7OrrbkN1+GdgRfIvDgTHx0V40XvH1bxz82PxH80we0IarcQKCNtSzMqDa2GJLB9wMmqFXCSDjiJFBw==")
      val response = client.request[Signature](
        Exchanges.KRAKEN,
        RequestTypes.SIGNED,
        HttpMethods.GET,
        "Kraken",
        nonceGenerator = nonceFn
      )

      response.onComplete {
        case Success(value) => assert(value == expectedSignature)
        case Failure(exception) => 
      }
    }

    it("should sign a Binance request") {
      val expectedSignature = Signature("e4d5a7fe32904551586a5161dbad46238e7b8533834850850869139819b489ec")
      val response = client.request[Signature](
        Exchanges.BINANCE,
        RequestTypes.SIGNED,
        HttpMethods.GET,
        "Binance",
        nonceGenerator = nonceFn
      )

      response.onComplete {
        case Success(value) => assert(value == expectedSignature)
        case Failure(exception) => 
      }
    }
  }
}