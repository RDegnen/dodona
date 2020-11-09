package dodona.backtester.routes

import scala.concurrent.ExecutionContext
import scala.util.Success

import _root_.dodona.backtester.models.SpreadModel
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import scala.util.Failure

class SpreadRoutes(implicit ec: ExecutionContext) {
  private val model = new SpreadModel()
  
  lazy val routes: Route = {
    path("spread") {
      onComplete(model.streamSpreads()) {
        case Failure(exception) => complete(exception)
        case Success(value) => handleWebSocketMessages(value)
      }
    }
  }
}