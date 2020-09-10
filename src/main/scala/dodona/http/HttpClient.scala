package dodona.http

import akka.actor.ActorSystem
import scala.concurrent.Future
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpMethod
import akka.stream.ActorMaterializer
import akka.http.scaladsl.unmarshalling.Unmarshal
import io.circe.Decoder
import io.circe.parser.decode
import scala.concurrent.Promise
import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.RequestEntity
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Query

trait IHttpClient {
  def request[T: Decoder](
    method: HttpMethod, 
    url: String,
    params: Map[String, String] = Map(),
    headers: Seq[HttpHeader] = Nil,
    entity: RequestEntity = HttpEntity.Empty
  ): Future[T]
}

class HttpClient(val baseUrl: String) extends IHttpClient {
  def request[T: Decoder](
    method: HttpMethod, 
    url: String,
    params: Map[String, String] = Map(),
    headers: Seq[HttpHeader] = Nil,
    entity: RequestEntity = HttpEntity.Empty
  ): Future[T] = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer
    implicit val executionContext = system.dispatcher
    val promise = Promise[T]
    val query = Query(params)

    val response = Http().singleRequest(
      HttpRequest(method, Uri(baseUrl ++: url).withQuery(query), headers, entity)
    )
    val unmarshalled = response.flatMap { response =>
      Unmarshal(response.entity).to[String]
    }

    unmarshalled.flatMap { value =>
      decode[T](value) match {
        case Right(t) => promise.success(t).future
        case Left(err) => promise.failure(err).future
      }
    }
  }
}