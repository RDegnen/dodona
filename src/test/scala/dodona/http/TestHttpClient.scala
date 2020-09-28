package dodona.http

import io.circe.Decoder
import akka.http.scaladsl.model.HttpMethod
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.RequestEntity
import akka.http.scaladsl.model.HttpEntity
import scala.concurrent.Future
import io.circe.parser.decode
import akka.actor.ActorSystem

class TestHttpClient extends IHttpClient {
  def sendRequest[T: Decoder](
      method: HttpMethod,
      url: String,
      query: Uri.Query = Query(),
      headers: Seq[HttpHeader] = Nil,
      entity: RequestEntity = HttpEntity.Empty
  ): Future[T] = {
    implicit val system = ActorSystem()
    implicit val executionContext = system.dispatcher
    var signature: String = ""

    url match {
      case "Kraken" => {
        val signatureValue = headers.map(header => header.value()).head
        signature = s"""{ "signature": "$signatureValue" }"""
      }
      case "Binance" => {
        val signatureValue = query.map(param => param._2).tail.head
        signature = s"""{ "signature": "$signatureValue" }"""
      }
    }
    
    Future {
      decode[T](signature) match {
        case Right(t) => t
      }
    }
  }
}