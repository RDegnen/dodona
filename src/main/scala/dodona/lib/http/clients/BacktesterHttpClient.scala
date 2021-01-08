package dodona.lib.http.clients

import scala.concurrent.{ExecutionContext, Future}

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.{HttpHeader, HttpMethod, RequestEntity}
import dodona.lib.http.{BaseHttpClient, CandlestickParams, HttpAuthLevel, HttpEndpoint, HttpEndpoints, QueryParameters}
import io.circe.Decoder

class BacktesterHttpClient extends BaseHttpClient {
  protected val baseUrl: String = "http://localhost:9090/api"
  private val candlesticksEndpoint = "/market/OHLC"

  def generateRequest[T: Decoder](
      authLevel: HttpAuthLevel,
      method: HttpMethod,
      endpoint: HttpEndpoint,
      params: QueryParameters,
      headers: Seq[HttpHeader],
      entity: RequestEntity
  )(implicit system: ActorSystem, ec: ExecutionContext): Future[T] = {
    endpoint match {
      case HttpEndpoints.CANDLESTICKS => {
        val url = s"$baseUrl$candlesticksEndpoint"
        params match {
          case CandlestickParams(pair, interval) =>
            executeRequest[T](
              method,
              url,
              Query(Map("symbol" -> pair, "interval" -> interval)),
              headers,
              entity
            )
        }
      }
      case HttpEndpoint(_) => ???
    }
  }
}
