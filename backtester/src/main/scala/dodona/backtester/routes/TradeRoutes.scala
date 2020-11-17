package dodona.backtester.routes

import scala.concurrent.ExecutionContext

import _root_.dodona.backtester.models.TradeModel
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

class TradeRoutes(implicit ec: ExecutionContext) {
  private val model = new TradeModel()

  lazy val routes: Route = {
    path("trade") {
      parameters("symbol", "timeToBegin".withDefault("0")) {
        (symbol, timeToBegin) =>
          handleWebSocketMessages(
            model.streamTradesBySymbolAndTime(symbol, timeToBegin.toLong)
          )
      }
    }
  }
}
