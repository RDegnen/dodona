package dodona.http

import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.{HttpEntity, HttpHeader, HttpMethod, HttpRequest, RequestEntity, Uri}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import io.circe.Decoder

class HttpClient(override val exchange: String, val baseUrl: String) extends IHttpClient(exchange) {
  def executeRequest[T: Decoder](
      method: HttpMethod,
      url: String,
      query: Uri.Query = Query(),
      headers: Seq[HttpHeader] = Nil,
      entity: RequestEntity = HttpEntity.Empty
  ): Future[T] = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer
    implicit val executionContext = system.dispatcher

    val response = Http().singleRequest(
      HttpRequest(
        method,
        Uri(baseUrl ++: url).withQuery(query),
        headers,
        entity
      )
    )
    val unmarshalled = response.flatMap { response =>
      Unmarshal(response.entity).to[String]
    }

    decodeResponse[T](unmarshalled)
  }
}
