package dodona.backtester.models

sealed trait CandlestickReturnType

final case class BinanceCandlestick(
  openTime:                 Long,
  open:                     BigDecimal,
  high:                     BigDecimal,
  low:                      BigDecimal,
  close:                    BigDecimal,
  volume:                   BigDecimal,
  closeTime:                Long,
  // quoteAssetVolume:         String,
  // numberOfTrades:           Long,
  // takerBuyBaseAssetVolume:  String,
  // takerBuyQuoteAssetVolume: String
) extends CandlestickReturnType

final case class KrakenCandlestick() extends CandlestickReturnType