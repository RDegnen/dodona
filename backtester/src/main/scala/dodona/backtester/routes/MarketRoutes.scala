package dodona.backtester.routes

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.Uri
import akka.actor.typed.ActorSystem
import _root_.dodona.backtester.actors.MainSystem
import scala.util.Success
import scala.util.Failure
import scala.concurrent.ExecutionContext

class MarketRoutes(implicit
    system: ActorSystem[MainSystem.Protocol],
    ec: ExecutionContext
) {
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
        }
      )
    }
  }
}
