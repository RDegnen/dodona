package dodona.lib.http.mappers

import dodona.constants.Exchanges

object DodonaEnpoints {
  final val CANDLESTICKS = "CANDLESTICKS"
  final val WEBSOCKET_TOKEN = "WEBSOCKET_TOKEN"
}

object EndpointsMapper {
  val binanceMap = Map(
    DodonaEnpoints.CANDLESTICKS -> "/api/v3/klines",
    DodonaEnpoints.WEBSOCKET_TOKEN -> "/api/v3/userDataStream"
  )

  val krakenMap = Map(
    DodonaEnpoints.CANDLESTICKS -> "/0/public/OHLC",
    DodonaEnpoints.WEBSOCKET_TOKEN -> "/0/private/GetWebSocketsToken"
  )

  val backtesterMap = Map(
    DodonaEnpoints.CANDLESTICKS -> "/candlesticks/OHLC"
  )

  def getEndpoint(exchange: String, endpoint: String): String = 
    exchange match {
      case Exchanges.BINANCE => binanceMap(endpoint)
      case Exchanges.KRAKEN => krakenMap(endpoint)
      case Exchanges.BACKTESTER => backtesterMap(endpoint)
    }
}