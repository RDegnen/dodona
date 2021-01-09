package dodona.lib.http.clients

import scala.concurrent.{ExecutionContext, Future}

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.{HttpHeader, HttpMethod, RequestEntity}
import dodona.lib.http.{BaseHttpClient, CANDLESTICKS, CandlestickParams, DefaultParams, HttpAuthLevel, HttpEndpoint, QueryParameters, WEBSOCKET_TOKEN}
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
    val paramsMap = convertParamsToMap(params)
    val url = generateUrl(endpoint)
    executeRequest[T](
      method,
      url,
      Query(paramsMap),
      headers,
      entity
    )
  }

  private def convertParamsToMap(params: QueryParameters): Map[String, String] =
    params match {
      case DefaultParams() => Map()
      case CandlestickParams(pair, interval) =>
        Map("symbol" -> pair, "interval" -> interval)
    }

  private def generateUrl(endpoint: HttpEndpoint): String =
    endpoint match {
      case CANDLESTICKS    => s"$baseUrl$candlesticksEndpoint"
      case WEBSOCKET_TOKEN => ???
    }
}
