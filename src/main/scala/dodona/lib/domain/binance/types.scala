package dodona.lib.domain.binance

case class ServerTime(serverTime: Long)

case class Symbol(symbol: String)

case class BinanceWsMessage(method: String, params: List[String], id: Int)

case class BinanceListenKey(listenKey: String)