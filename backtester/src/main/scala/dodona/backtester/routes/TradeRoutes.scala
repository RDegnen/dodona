package dodona.backtester.routes

import scala.concurrent.ExecutionContext

import _root_.dodona.backtester.actors.MainSystem
import _root_.dodona.backtester.models.TradeModel
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

class TradeRoutes(implicit system: ActorSystem[MainSystem.Protocol], ec: ExecutionContext) {
  private val model = TradeModel()

  lazy val webSocketRoutes: Route = {
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
