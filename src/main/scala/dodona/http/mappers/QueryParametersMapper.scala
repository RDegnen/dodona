package dodona.http.mappers

import dodona.constants.Exchanges
import dodona.domain.dodona.http.{CandlestickParams, DefaultParams, QueryParameters}

object QueryParametersMapper {
  def binanceMatcher(params: QueryParameters): Map[String, String] =
    params match {
      case DefaultParams() => Map()
      case CandlestickParams(pair, interval) =>
        Map("symbol" -> pair, "interval" -> interval)
    }

  def krakenMatcher(params: QueryParameters): Map[String, String] =
    params match {
      case DefaultParams() => Map()
      case CandlestickParams(pair, interval) =>
        Map("pair" -> pair, "interval" -> interval)
    }

  def convertParamsToMap(exchange: String, params: QueryParameters): Map[String, String] =
    exchange match {
      case Exchanges.BINANCE => binanceMatcher(params)
      case Exchanges.KRAKEN => krakenMatcher(params)
    }
}