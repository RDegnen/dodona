package dodona.backtester.lib.http

import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.{HttpEntity, HttpHeader, HttpMethod, RequestEntity, Uri}
import dodona.backtester.controllers.CandlesticksController
import dodona.http.IHttpClient
import io.circe.Decoder
import scala.concurrent.ExecutionContext

class HttpClient(override val exchange: String, val baseUrl: String) extends IHttpClient(exchange) {
  private val candlesticksController = new CandlesticksController(exchange)

  def executeRequest[T: Decoder](
      method: HttpMethod,
      url: String,
      query: Uri.Query = Query(),
      headers: Seq[HttpHeader] = Nil,
      entity: RequestEntity = HttpEntity.Empty
  )(implicit system: ActorSystem, ec: ExecutionContext): Future[T] = {
    url match {
      case "/api/v3/klines" => {
        val response = candlesticksController.getBatch()
        decodeResponse[T](response)
      }
    }
  }
}
