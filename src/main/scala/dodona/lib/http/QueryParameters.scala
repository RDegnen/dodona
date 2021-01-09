package dodona.lib.http

sealed trait QueryParameters

case class DefaultParams() extends QueryParameters
case class CandlestickParams(pair: String, interval: String) extends QueryParameters

