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
import dodona.constants.Exchanges
import java.security.MessageDigest
import akka.http.scaladsl.model.headers.RawHeader
import org.apache.commons.codec.binary.Base64
import akka.http.scaladsl.model.FormData

trait IHttpClient {
  def sendRequest[T: Decoder](
      method: HttpMethod,
      url: String,
      query: Uri.Query,
      headers: Seq[HttpHeader] = Nil,
      entity: RequestEntity = HttpEntity.Empty
  ): Future[T]

  def request[T: Decoder](
      exchange: String,
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
      case RequestTypes.SIGNED =>
        exchange match {
          case Exchanges.BINANCE => {
            val timestamp = System.currentTimeMillis().toString()
            val paramsWithTimestamp =
              params.concat(Map("timestamp" -> timestamp))
            val signature = generateHMAC(
              "HmacSHA256",
              Query(paramsWithTimestamp).toString(),
              DodonaConfig.BINANCE_US_SECRET
            )
            val newQuery = Query(
              paramsWithTimestamp.concat(Map("signature" -> signature))
            )
            sendRequest[T](method, url, newQuery, headers, entity)
          }
          case Exchanges.KRAKEN => {
            val nonce = System.currentTimeMillis()
            val parameters = params.concat(Map("nonce" -> nonce.toString()))
            val signature = krakenSig(url, nonce, Query(parameters).toString(), DodonaConfig.KRAKEN_SECRET)
            val newHeaders = headers :+ RawHeader("API-Sign", signature)
            sendRequest[T](method, url, Query(params), newHeaders, FormData(Map("nonce" -> nonce.toString)).toEntity)
          }
        }
    }

  private def generateHMAC(
      hmac: String,
      message: String,
      secret: String
  ): String = {
    val sha256HMAC = Mac.getInstance(hmac)
    val secretKeySpec = new SecretKeySpec(secret.getBytes, hmac)
    sha256HMAC.init(secretKeySpec)
    new String(Hex.encodeHex(sha256HMAC.doFinal(message.getBytes)))
  }

  private def krakenSig(path: String, nonce: Long, postData: String, apiSecret: String): String = {
    val md = MessageDigest.getInstance("SHA-256")
    md.update((nonce + postData).getBytes())
    println(nonce + postData)
    val mac = Mac.getInstance("HmacSHA512")
    mac.init(new SecretKeySpec(Base64.decodeBase64(apiSecret), "HmacSHA512"))
    mac.update(path.getBytes())
    new String(Base64.encodeBase64(mac.doFinal(md.digest())))
  }
}
