package dodona.backtester.lib.http

import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.{HttpEntity, HttpHeader, HttpMethod, RequestEntity, Uri}
import dodona.backtester.controllers.CandlesticksController
import dodona.http.IHttpClient
import io.circe.Decoder

class HttpClient(override val exchange: String, val baseUrl: String) extends IHttpClient(exchange) {
  implicit val system = ActorSystem()
  implicit val executionContext = system.dispatcher
  private val candlesticksController = new CandlesticksController(exchange)

  def executeRequest[T: Decoder](
      method: HttpMethod,
      url: String,
      query: Uri.Query = Query(),
      headers: Seq[HttpHeader] = Nil,
      entity: RequestEntity = HttpEntity.Empty
  ): Future[T] = {
    url match {
      case "/api/v3/klines" => {
        val response = candlesticksController.getBatch()
        decodeResponse[T](response)
      }
    }
  }
}
