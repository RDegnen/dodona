package dodona.backtester.controllers

import scala.concurrent.{ExecutionContext, Future}

import dodona.backtester.lib.json.Encoders._
import dodona.backtester.models.CandlestickModel
import io.circe.syntax._

class CandlesticksController(exchange: String) {
  private val model = new CandlestickModel(exchange)

  def getBatch()(implicit ec: ExecutionContext): Future[String] = {
    val candlesticksFuture = model.getCandlesticks()
    candlesticksFuture.flatMap(seq =>
      Future { seq.asJson.toString }
    )
  }
}
