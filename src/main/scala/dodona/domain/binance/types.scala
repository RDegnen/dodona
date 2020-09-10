package dodona.domain.binance

case class ServerTime(serverTime: Long)
case class TickerPrice(symbol: String, price: BigDecimal)
case class Symbol(symbol: String)