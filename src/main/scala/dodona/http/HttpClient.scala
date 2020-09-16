package dodona.http

import akka.actor.ActorSystem
import scala.concurrent.Future
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpMethod
import akka.stream.ActorMaterializer
import akka.http.scaladsl.unmarshalling.Unmarshal
import io.circe.Decoder
import io.circe.parser.decode
import scala.concurrent.Promise
import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.RequestEntity
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Query
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import dodona.DodonaConfig
import dodona.domain.binance.ServerTime
import akka.http.scaladsl.model.HttpMethods
import dodona.json.binance.Decoders._
import org.apache.commons.codec.binary.Hex

trait IHttpClient {
  def sendRequest[T: Decoder](
      method: HttpMethod,
      url: String,
      query: Uri.Query,
      headers: Seq[HttpHeader] = Nil,
      entity: RequestEntity = HttpEntity.Empty
  ): Future[T]

  def getServerTime(): Future[ServerTime]

  def request[T: Decoder](
      requestType: String,
      method: HttpMethod,
      url: String,
      params: Map[String, String] = Map(),
      headers: Seq[HttpHeader] = Nil,
      entity: RequestEntity = HttpEntity.Empty
  ): Future[T] =
    requestType match {
      case "public" => sendRequest[T](method, url, Query(params), headers, entity)
      case "signed" => {
        implicit val system = ActorSystem()
        implicit val executionContext = system.dispatcher
        val serverTime = getServerTime()
        val requestFuture = serverTime
          .map { value =>
            val timestamp = value.serverTime.toString()
            val paramsWithTimestamp =
              params.concat(Map("timestamp" -> timestamp))
            val signature = generateHMAC(
              Query(paramsWithTimestamp).toString(),
              DodonaConfig.BINANCE_US_SECRET
            )
            val newQuery =
              Query(paramsWithTimestamp.concat(Map("signature" -> signature)))
            println(newQuery)
            sendRequest[T](method, url, newQuery, headers, entity)
          }
        requestFuture.flatMap { future =>
          future
        }
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

class HttpClient(val baseUrl: String) extends IHttpClient {
  def sendRequest[T: Decoder](
      method: HttpMethod,
      url: String,
      query: Uri.Query = Query(),
      headers: Seq[HttpHeader] = Nil,
      entity: RequestEntity = HttpEntity.Empty
  ): Future[T] = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer
    implicit val executionContext = system.dispatcher
    val promise = Promise[T]

    val response = Http().singleRequest(
      HttpRequest(
        method,
        Uri(baseUrl ++: url).withQuery(query),
        headers,
        entity
      )
    )
    val unmarshalled = response.flatMap { response =>
      Unmarshal(response.entity).to[String]
    }

    unmarshalled.flatMap { value =>
      println(value)
      decode[T](value) match {
        case Right(t)  => promise.success(t).future
        case Left(err) => promise.failure(err).future
      }
    }
  }

  def getServerTime(): Future[ServerTime] =
    sendRequest[ServerTime](HttpMethods.GET, "/api/v3/time")
}
