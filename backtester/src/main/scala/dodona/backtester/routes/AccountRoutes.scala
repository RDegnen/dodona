package dodona.backtester.routes

import scala.concurrent.ExecutionContext

import _root_.dodona.backtester.models.account.WalletModel
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.circe.syntax._

class AccountRoutes(implicit ec: ExecutionContext) {
  private val walletModel = new WalletModel()

  lazy val apiRoutes: Route = {
    pathPrefix("account") {
      path("balance") {
        parameters("symbol") { symbol =>
          get {
            complete(walletModel.getBalance(symbol).asJson.toString())
          }
        }
      }
    }
  }
}