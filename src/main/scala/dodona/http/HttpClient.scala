package dodona.http

import akka.actor.ActorSystem
import scala.concurrent.Future
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpMethod
import scala.util.Success
import scala.util.Failure
import akka.stream.ActorMaterializer
import akka.http.scaladsl.unmarshalling.Unmarshal
import io.circe.Decoder
import io.circe.generic.semiauto._
import io.circe.parser.decode
import scala.concurrent.Promise
import akka.http.scaladsl.model.HttpHeader

trait IHttpClient {
  def request[T: Decoder](
    method: HttpMethod, 
    url: String,
    headers: Seq[HttpHeader] = Nil
  ): Future[T]
}

class HttpClient(val baseUrl: String) extends IHttpClient {
  def request[T: Decoder](
    method: HttpMethod, 
    url: String,
    headers: Seq[HttpHeader] = Nil
  ): Future[T] = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer
    implicit val executionContext = system.dispatcher
    val promise = Promise[T]

    val response = Http().singleRequest(
      HttpRequest(method, baseUrl ++: url, headers)
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