package dodona.http

import scala.concurrent.Future
import akka.http.scaladsl.model.HttpMethod
import io.circe.Decoder
import io.circe.parser.decode
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
import dodona.http.mappers.EndpointsMapper
import dodona.domain.dodona.http.QueryParameters
import dodona.domain.dodona.http.DefaultParams
import dodona.http.mappers.QueryParametersMapper
import scala.concurrent.ExecutionContext
import scala.concurrent.Promise

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
      params: QueryParameters = DefaultParams(),
      headers: Seq[HttpHeader] = Seq(),
      entity: RequestEntity = HttpEntity.Empty,
      nonceGenerator: () => Long = () => System.currentTimeMillis()
  ): Future[T] = {
    val endpoint = EndpointsMapper.getEndpoint(exchange, url)
    val paramsMap = QueryParametersMapper.convertParamsToMap(exchange, params)

    requestType match {
      case RequestTypes.PUBLIC =>
        executeRequest[T](method, endpoint, Query(paramsMap), headers, entity)
      case RequestTypes.SIGNED =>
        exchange match {
          case Exchanges.BINANCE => {
            val paramsWithTimestamp =
              paramsMap.concat(Map("timestamp" -> nonceGenerator().toString()))
            val signature = HttpClientUtils.binanceSignature(
              Query(paramsWithTimestamp).toString(),
              DodonaConfig.BINANCE_US_SECRET
            )
            val newQuery = Query(
              paramsWithTimestamp.concat(Map("signature" -> signature))
            )
            val newHeaders =
              headers :+ RawHeader("X-MBX-APIKEY", DodonaConfig.BINANCE_US_KEY)
            executeRequest[T](method, endpoint, newQuery, newHeaders, entity)
          }
          case Exchanges.KRAKEN => {
            val nonce = nonceGenerator()
            val parameters = paramsMap.concat(Map("nonce" -> nonce.toString()))
            val signature = HttpClientUtils.krakenSignature(
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
              endpoint,
              Query(paramsMap),
              newHeaders,
              FormData(Map("nonce" -> nonce.toString)).toEntity
            )
          }
        }
    }
  }

  def decodeResponse[T: Decoder](jsonFuture: Future[String])(implicit ec: ExecutionContext): Future[T] = {
    val promise = Promise[T]
    jsonFuture.flatMap(value => {
      decode[T](value) match {
        case Right(decoded) => promise.success(decoded).future
        case Left(err) => promise.failure(err).future
      }
    })
  }
}
