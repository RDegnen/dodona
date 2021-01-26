package dodona.lib.http.clients

import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import scala.concurrent.{ExecutionContext, Future}

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{FormData, HttpHeader, HttpMethod, RequestEntity}
import dodona.lib.http.{BaseHttpClient, HttpAuthLevel, PUBLIC, SIGNED}
import dodona.{DodonaConfig, MainSystem}
import io.circe.Decoder
import org.apache.commons.codec.binary.Base64

class KrakenHttpClient extends BaseHttpClient {
  protected val baseUrl: String = "https://api.kraken.com"

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
        val nonce = nonceGenerator()
        val parameters = params.concat(Map("nonce" -> nonce.toString()))
        val signature = generateSignature(
          endpoint,
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
          Query(params),
          newHeaders,
          FormData(Map("nonce" -> nonce.toString)).toEntity
        )
      }
    }
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
}
