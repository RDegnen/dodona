package dodona.backtester.routes

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

import _root_.dodona.backtester.actors.MainSystem
import _root_.dodona.backtester.models.{CandlestickModel, TradeModel}
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, Uri}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.circe.syntax._

class MarketRoutes(implicit
    system: ActorSystem[MainSystem.Protocol],
    ec: ExecutionContext
) {
  private val tradeModel = TradeModel()
  private val candleStickModel = new CandlestickModel()

  lazy val apiRoutes: Route = {
    pathPrefix("market") {
      concat(
        path("exchangeInfo") {
          get {
            // Get this from Binance
            val response = Http().singleRequest(
              HttpRequest(
                HttpMethods.GET,
                Uri("https://api.binance.us/api/v3/exchangeInfo")
              )
            )
            onComplete(response) {
              case Failure(exception) => complete(exception)
              case Success(value)     => complete(value)
            }
          }
        },
        path("OHLC") {
          parameters("symbol") { symbol =>
            get {
              onComplete(candleStickModel.getOHLCbySymbol(symbol)) {
                case Failure(exception) => complete(exception)
                case Success(value) => complete(value.asJson.toString())
              }
            }
          }
        }
      )
    }
  }

  lazy val webSocketRoutes: Route = {
    pathPrefix("market") {
      concat(
        path("trade") {
          parameters("symbol", "timeToBegin".withDefault("0")) {
            (symbol, timeToBegin) =>
              handleWebSocketMessages(
                tradeModel.streamTradesBySymbolAndTime(
                  symbol,
                  timeToBegin.toLong
                )
              )
          }
        }
      )
    }
  }
}
