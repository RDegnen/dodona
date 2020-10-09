package dodona.http

import scala.concurrent.Future
import akka.http.scaladsl.model.HttpMethod
import io.circe.Decoder
import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.RequestEntity
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Query
import dodona.DodonaConfig
import dodona.constants.RequestTypes
import dodona.constants.Exchanges
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.FormData

abstract class IHttpClient(val exchange: String) {
  def executeRequest[T: Decoder](
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
      entity: RequestEntity = HttpEntity.Empty,
      nonceGenerator: () => Long = () => System.currentTimeMillis()
  ): Future[T] =
    requestType match {
      case RequestTypes.PUBLIC =>
        executeRequest[T](method, url, Query(params), headers, entity)
      case RequestTypes.SIGNED =>
        exchange match {
          case Exchanges.BINANCE => {
            val paramsWithTimestamp =
              params.concat(Map("timestamp" -> nonceGenerator().toString()))
            val signature = HttpClientUtils.binanceSignature(
              Query(paramsWithTimestamp).toString(),
              DodonaConfig.BINANCE_US_SECRET
            )
            val newQuery = Query(
              paramsWithTimestamp.concat(Map("signature" -> signature))
            )
            executeRequest[T](method, url, newQuery, headers, entity)
          }
          case Exchanges.KRAKEN => {
            val nonce = nonceGenerator()
            val parameters = params.concat(Map("nonce" -> nonce.toString()))
            val signature = HttpClientUtils.krakenSignature(
              url,
              nonce,
              Query(parameters).toString(),
              DodonaConfig.KRAKEN_SECRET
            )
            val newHeaders = headers :+ RawHeader("API-Sign", signature)
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
}
