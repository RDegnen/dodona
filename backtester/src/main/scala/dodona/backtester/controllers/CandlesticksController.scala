package dodona.backtester.controllers

import dodona.backtester.models.CandlestickModel
import dodona.backtester.lib.json.Encoders._
import io.circe.syntax._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

class CandlesticksController(exchange: String) {
  private val model = new CandlestickModel(exchange)

  def getBatch()(implicit ec: ExecutionContext): Future[String] = {
    val candlesticksFuture = model.getCandlesticks()
    candlesticksFuture.flatMap(seq =>
      Future { seq.asJson.toString }
    )
  }
}
