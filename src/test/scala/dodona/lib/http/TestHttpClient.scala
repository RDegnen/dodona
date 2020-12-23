package dodona.lib.http

import io.circe.Decoder
import akka.http.scaladsl.model.HttpMethod
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.RequestEntity
import akka.http.scaladsl.model.HttpEntity
import scala.concurrent.Future
import akka.actor.ActorSystem
import scala.concurrent.ExecutionContext

class TestHttpClient(override val exchange: String) extends IHttpClient(exchange) {
  def executeRequest[T: Decoder](
      method: HttpMethod,
      url: String,
      query: Uri.Query = Query(),
      headers: Seq[HttpHeader] = Nil,
      entity: RequestEntity = HttpEntity.Empty
  )(implicit system: ActorSystem, ec: ExecutionContext): Future[T] = {
    var signature: String = ""

    url match {
      case "/0/public/OHLC" => {
        val signatureValue = headers.map(header => header.value()).head
        signature = s"""{ "signature": "$signatureValue" }"""
      }
      case "/api/v3/klines" => {
        val signatureValue = query.map(param => param._2).tail.head
        signature = s"""{ "signature": "$signatureValue" }"""
      }
    }
    
    decodeResponse[T](Future { signature })
  }
}