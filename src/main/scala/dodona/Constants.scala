package dodona

object Constants {
  // Backtester
  final val BACKTESTER_WS_URL = "ws://localhost:9090"
  // Binance
  final val BINANCE_WS_RAW_STREAM_BASE_URL = "wss://stream.binance.us:9443/ws"
  final val BINANCE_WS_COMBINED_STREAM_BASE_URL = "wss://stream.binance.us:9443/stream?"
  // Kraken
  final val KRAKEN_PUBLIC_WS_URL = "wss://ws.kraken.com"
  final val KRAKEN_PRIVATE_WS_URL = "wss://ws-auth.kraken.com"

  object OrderSides {
    final val BUY = "BUY"
    final val SELL = "SELL"
  }
}
