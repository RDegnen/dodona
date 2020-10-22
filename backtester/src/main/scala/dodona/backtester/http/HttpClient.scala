package dodona.backtester.http

import dodona.http.IHttpClient
import io.circe.Decoder
import akka.http.scaladsl.model.HttpMethod
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.RequestEntity
import akka.http.scaladsl.model.HttpEntity
import scala.concurrent.Future

class HttpClient(override val exchange: String, val baseUrl: String) extends IHttpClient(exchange) {
    def executeRequest[T: Decoder](
      method: HttpMethod,
      url: String,
      query: Uri.Query = Query(),
      headers: Seq[HttpHeader] = Nil,
      entity: RequestEntity = HttpEntity.Empty
  ): Future[T] = ???
}
