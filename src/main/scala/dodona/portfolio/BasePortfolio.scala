package dodona.portfolio

import dodona.lib.http.BaseHttpClient

abstract class BasePortfolio(quoteAsset: String) extends IPortfolio {
  protected val httpClient: BaseHttpClient

  protected def splitPair(pair: String): Option[(String, String)] = {
    val quoteIdx = pair.indexOf(quoteAsset)
    if (quoteIdx > -1) {
      val quote = pair.slice(quoteIdx, pair.length)
      val base = pair.slice(0, quoteIdx)
      Some(base, quote)
    } else {
      None
    }
  }
}