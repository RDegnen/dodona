package dodona.backtester.lib

object Pairs {
  private val pairsMap = Map(
    "ETHUSD" -> ("ETH", "USD")
  )

  def getPair(symbol: String): (String, String) = pairsMap.get(symbol) match {
    case Some(value) => value
    case None => ("", "")
  }
}
