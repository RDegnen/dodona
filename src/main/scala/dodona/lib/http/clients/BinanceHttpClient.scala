package dodona.lib.http.clients

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import scala.concurrent.{ExecutionContext, Future}

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpHeader, HttpMethod, RequestEntity}
import dodona.DodonaConfig
import dodona.lib.http.{BaseHttpClient, CANDLESTICKS, CandlestickParams, DefaultParams, HttpAuthLevel, HttpEndpoint, PUBLIC, QueryParameters, SIGNED, WEBSOCKET_TOKEN}
import io.circe.Decoder
import org.apache.commons.codec.binary.Hex

class BinanceHttpClient extends BaseHttpClient {
  protected val baseUrl: String = "https://api.binance.us"
  private val candlesticksEndpoint = "/api/v3/klines"
  private val websocketTokenEndpoint = "/api/v3/userDataStream"

  def generateRequest[T: Decoder](
      authLevel: HttpAuthLevel,
      method: HttpMethod,
      endpoint: HttpEndpoint,
      params: QueryParameters,
      headers: Seq[HttpHeader],
      entity: RequestEntity
  )(implicit system: ActorSystem, ec: ExecutionContext): Future[T] = {
    val paramsMap = convertParamsToMap(params)
    authLevel match {
      case PUBLIC => {
        val url = generateUrl(endpoint)
        executeRequest[T](method, url, Query(paramsMap), headers, entity)
      }
      case SIGNED => {
        val paramsWithTimestamp =
          paramsMap.concat(Map("timestamp" -> nonceGenerator().toString()))
        val signature = generateSignature(
          Query(paramsWithTimestamp).toString(),
          DodonaConfig.BINANCE_US_SECRET
        )
        val newQuery = Query(
          paramsWithTimestamp.concat(Map("signature" -> signature))
        )
        val newHeaders =
          headers :+ RawHeader("X-MBX-APIKEY", DodonaConfig.BINANCE_US_KEY)
        val url = generateUrl(endpoint)
        executeRequest[T](method, url, newQuery, newHeaders, entity)
      }
    }
  }

  private def convertParamsToMap(params: QueryParameters): Map[String, String] =
    params match {
      case DefaultParams() => Map()
      case CandlestickParams(pair, interval) =>
        Map("symbol" -> pair, "interval" -> interval)
    }

  private def generateSignature(message: String, secret: String): String = {
    val hmacSHA256 = "HmacSHA256"
    val mac = Mac.getInstance(hmacSHA256)
    mac.init(new SecretKeySpec(secret.getBytes, hmacSHA256))
    new String(Hex.encodeHex(mac.doFinal(message.getBytes)))
  }

  private def generateUrl(endpoint: HttpEndpoint): String =
    endpoint match {
      case CANDLESTICKS    => s"$baseUrl$candlesticksEndpoint"
      case WEBSOCKET_TOKEN => s"$baseUrl$websocketTokenEndpoint"
    }
}
