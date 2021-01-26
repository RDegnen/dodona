package dodona.lib.http.clients

import scala.concurrent.{ExecutionContext, Future}

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.{HttpHeader, HttpMethod, RequestEntity}
import dodona.MainSystem
import dodona.lib.http.{BaseHttpClient, HttpAuthLevel}
import io.circe.Decoder

class BacktesterHttpClient extends BaseHttpClient {
  protected val baseUrl: String = "http://localhost:9090/api"

  def generateRequest[T: Decoder](
      authLevel: HttpAuthLevel,
      method: HttpMethod,
      endpoint: String,
      params: Map[String, String],
      headers: Seq[HttpHeader],
      entity: RequestEntity
  )(implicit system: ActorSystem[MainSystem.Protocol], ec: ExecutionContext): Future[T] = {
    val url = s"$baseUrl$endpoint"
    executeRequest[T](
      method,
      url,
      Query(params),
      headers,
      entity
    )
  }
}
