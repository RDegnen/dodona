package dodona.backtester.routes

import scala.concurrent.ExecutionContext

import _root_.dodona.backtester.lib.json.Encoders._
import _root_.dodona.backtester.models.CandlestickModel
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.circe.syntax._
import scala.util.Success
import scala.util.Failure

class CandlestickRoutes(implicit ec: ExecutionContext) {
  private val model = new CandlestickModel()

  lazy val routes: Route = {
    pathPrefix("candlesticks") {
      concat(
        path("OHLC") {
          get {
            onComplete(model.getOHLC()) {
              case Failure(exception) => complete(exception)
              case Success(value) => complete(value.asJson.toString())
            }
          }
        }
      )
    }
  }
}