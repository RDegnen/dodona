package dodona.backtester.routes

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import _root_.dodona.backtester.models.CandlestickModel
import _root_.dodona.backtester.lib.json.Encoders._
import scala.concurrent.ExecutionContext
import io.circe.syntax._
import scala.concurrent.Future

class CandlestickRoutes(implicit ec: ExecutionContext) {
  private val model = new CandlestickModel()

  lazy val routes: Route = {
    pathPrefix("candlesticks") {
      concat(
        path("OHLC") {
          get {
            complete {
              model.getOHLC().flatMap(seq =>
                Future { seq.asJson.toString() }
              )
            }
          }
        }
      )
    }
  }
}