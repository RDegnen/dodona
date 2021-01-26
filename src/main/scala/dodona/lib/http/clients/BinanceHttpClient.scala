package dodona.lib.http.clients

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import scala.concurrent.{ExecutionContext, Future}

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpHeader, HttpMethod, RequestEntity}
import dodona.lib.http.{BaseHttpClient, HttpAuthLevel, PUBLIC, SIGNED}
import dodona.{DodonaConfig, MainSystem}
import io.circe.Decoder
import org.apache.commons.codec.binary.Hex

class BinanceHttpClient extends BaseHttpClient {
  protected val baseUrl: String = "https://api.binance.us"

  def generateRequest[T: Decoder](
      authLevel: HttpAuthLevel,
      method: HttpMethod,
      endpoint: String,
      params: Map[String, String],
      headers: Seq[HttpHeader],
      entity: RequestEntity
  )(implicit system: ActorSystem[MainSystem.Protocol], ec: ExecutionContext): Future[T] = {
    val url = s"$baseUrl$endpoint"
    authLevel match {
      case PUBLIC => {
        executeRequest[T](method, url, Query(params), headers, entity)
      }
      case SIGNED => {
        val paramsWithTimestamp =
          params.concat(Map("timestamp" -> nonceGenerator().toString()))
        val signature = generateSignature(
          Query(paramsWithTimestamp).toString(),
          DodonaConfig.BINANCE_US_SECRET
        )
        val newQuery = Query(
          paramsWithTimestamp.concat(Map("signature" -> signature))
        )
        val newHeaders =
          headers :+ RawHeader("X-MBX-APIKEY", DodonaConfig.BINANCE_US_KEY)
        executeRequest[T](method, url, newQuery, newHeaders, entity)
      }
    }
  }

  private def generateSignature(message: String, secret: String): String = {
    val hmacSHA256 = "HmacSHA256"
    val mac = Mac.getInstance(hmacSHA256)
    mac.init(new SecretKeySpec(secret.getBytes, hmacSHA256))
    new String(Hex.encodeHex(mac.doFinal(message.getBytes)))
  }
}
