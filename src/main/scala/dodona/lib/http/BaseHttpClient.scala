package dodona.lib.http

import scala.concurrent.{ExecutionContext, Future, Promise}

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.{HttpEntity, HttpHeader, HttpMethod, HttpRequest, RequestEntity, Uri}
import akka.http.scaladsl.unmarshalling.Unmarshal
import io.circe.Decoder
import io.circe.parser.decode
import dodona.MainSystem
import com.typesafe.scalalogging.LazyLogging

trait BaseHttpClient extends LazyLogging {
  protected val baseUrl: String

  def generateRequest[T: Decoder](
    authLevel: HttpAuthLevel,
    method: HttpMethod,
    endpoint: String,
    params: Map[String, String] = Map(),
    headers: Seq[HttpHeader] = Seq(),
    entity: RequestEntity = HttpEntity.Empty,
  )(implicit system: ActorSystem[MainSystem.Protocol], ec: ExecutionContext): Future[T]

  protected def nonceGenerator: () => Long = () => System.currentTimeMillis()
  
  protected def executeRequest[T: Decoder](
      method: HttpMethod,
      url: String,
      query: Query = Query(),
      headers: Seq[HttpHeader] = Nil,
      entity: RequestEntity = HttpEntity.Empty
  )(implicit system: ActorSystem[MainSystem.Protocol], ec: ExecutionContext): Future[T] = {
    val response = Http().singleRequest(
      HttpRequest(
        method,
        Uri(url).withQuery(query),
        headers,
        entity
      )
    )
    // FIXME need far better error handling here
    val unmarshalled = response.flatMap { response =>
      Unmarshal(response.entity).to[String]
    }

    decodeResponse[T](unmarshalled)
  }

  private def decodeResponse[T: Decoder](jsonFuture: Future[String])(implicit ec: ExecutionContext): Future[T] = {
    val promise = Promise[T]
    jsonFuture.flatMap(value => {
      decode[T](value) match {
        case Right(decoded) => promise.success(decoded).future
        case Left(err) =>
          logger.error(err.getMessage)
          promise.failure(err).future
      }
    })
  }
}