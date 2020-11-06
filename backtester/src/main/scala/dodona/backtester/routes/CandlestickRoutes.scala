package dodona.backtester.routes

import scala.concurrent.{ExecutionContext, Future}

import _root_.dodona.backtester.lib.json.Encoders._
import _root_.dodona.backtester.models.CandlestickModel
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.circe.syntax._

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