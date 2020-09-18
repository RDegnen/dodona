package dodona.http

import scala.concurrent.Future
import akka.http.scaladsl.model.HttpMethod
import io.circe.Decoder
import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.RequestEntity
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Query
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import dodona.DodonaConfig
import org.apache.commons.codec.binary.Hex
import dodona.constants.RequestTypes

trait IHttpClient {
  def sendRequest[T: Decoder](
      method: HttpMethod,
      url: String,
      query: Uri.Query,
      headers: Seq[HttpHeader] = Nil,
      entity: RequestEntity = HttpEntity.Empty
  ): Future[T]

  def request[T: Decoder](
      requestType: String,
      method: HttpMethod,
      url: String,
      params: Map[String, String] = Map(),
      headers: Seq[HttpHeader] = Nil,
      entity: RequestEntity = HttpEntity.Empty
  ): Future[T] =
    requestType match {
      case RequestTypes.PUBLIC =>
        sendRequest[T](method, url, Query(params), headers, entity)
      case RequestTypes.SIGNED => {
        val timestamp = System.currentTimeMillis().toString()
        val paramsWithTimestamp = params.concat(Map("timestamp" -> timestamp))
        val signature = generateHMAC(
          Query(paramsWithTimestamp).toString(),
          DodonaConfig.BINANCE_US_SECRET
        )
        val newQuery = Query(
          paramsWithTimestamp.concat(Map("signature" -> signature))
        )
        sendRequest[T](method, url, newQuery, headers, entity)
      }
    }

  private def generateHMAC(message: String, secret: String): String = {
    val hmac = "HmacSHA256"
    val sha256HMAC = Mac.getInstance(hmac)
    val secretKeySpec = new SecretKeySpec(secret.getBytes, hmac)
    sha256HMAC.init(secretKeySpec)
    new String(Hex.encodeHex(sha256HMAC.doFinal(message.getBytes)))
  }
}