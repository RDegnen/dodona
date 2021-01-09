package dodona.lib.http.clients

import dodona.lib.http.BaseHttpClient
import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpHeader, HttpMethod, RequestEntity}
import dodona.lib.http.{HttpAuthLevel, HttpEndpoint, QueryParameters}
import scala.concurrent.{ExecutionContext, Future}
import io.circe.Decoder
import dodona.lib.http.CANDLESTICKS
import dodona.lib.http.WEBSOCKET_TOKEN
import dodona.lib.http.DefaultParams
import dodona.lib.http.CandlestickParams
import dodona.lib.http.PUBLIC
import akka.http.scaladsl.model.Uri.Query
import dodona.lib.http.SIGNED
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.apache.commons.codec.binary.Base64
import dodona.DodonaConfig
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.FormData

class KrakenHttpClient extends BaseHttpClient {
  protected val baseUrl: String = "https://api.kraken.com"
  private val candlesticksEndpoint = "/0/public/OHLC"
  private val websocketTokenEndpoint = "/0/private/GetWebSocketsToken"

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
    authLevel match {
      case PUBLIC => {
        executeRequest[T](method, url, Query(paramsMap), headers, entity)
      }
      case SIGNED => {
        val nonce = nonceGenerator()
        val parameters = paramsMap.concat(Map("nonce" -> nonce.toString()))
        val signature = generateSignature(
          mapEndpointToString(endpoint),
          nonce,
          Query(parameters).toString(),
          DodonaConfig.KRAKEN_SECRET
        )
        val newHeaders = headers ++ Seq(
          RawHeader("API-Sign", signature),
          RawHeader("API-Key", DodonaConfig.KRAKEN_KEY)
        )
        executeRequest[T](
          method,
          url,
          Query(paramsMap),
          newHeaders,
          FormData(Map("nonce" -> nonce.toString)).toEntity
        )
      }
    }
  }

  private def convertParamsToMap(params: QueryParameters): Map[String, String] =
    params match {
      case DefaultParams() => Map()
      case CandlestickParams(pair, interval) =>
        Map("pair" -> pair, "interval" -> interval)
    }

  def generateSignature(
      path: String,
      nonce: Long,
      postData: String,
      apiSecret: String
  ): String = {
    val hmacSHA512 = "HmacSHA512"
    val md = MessageDigest.getInstance("SHA-256")
    md.update((nonce + postData).getBytes())
    val mac = Mac.getInstance(hmacSHA512)
    mac.init(new SecretKeySpec(Base64.decodeBase64(apiSecret), hmacSHA512))
    mac.update(path.getBytes())
    new String(Base64.encodeBase64(mac.doFinal(md.digest())))
  }

  private def generateUrl(endpoint: HttpEndpoint): String =
    endpoint match {
      case CANDLESTICKS    => s"$baseUrl$candlesticksEndpoint"
      case WEBSOCKET_TOKEN => s"$baseUrl$websocketTokenEndpoint"
    }

  private def mapEndpointToString(endpoint: HttpEndpoint): String =
    endpoint match {
      case CANDLESTICKS    => candlesticksEndpoint
      case WEBSOCKET_TOKEN => websocketTokenEndpoint
    }
}
