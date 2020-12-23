package dodona.backtester.routes

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

import _root_.dodona.backtester.models.CandlestickModel
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.circe.syntax._

class CandlestickRoutes(implicit ec: ExecutionContext) {
  private val model = new CandlestickModel()

  lazy val apiRoutes: Route = {
    pathPrefix("candlesticks") {
      concat(
        path("OHLC") {
          parameters("symbol") { symbol =>
            get {
              onComplete(model.getOHLCbySymbol(symbol)) {
                case Failure(exception) => complete(exception)
                case Success(value) => complete(value.asJson.toString())
              }
            }
          }
        }
      )
    }
  }
}