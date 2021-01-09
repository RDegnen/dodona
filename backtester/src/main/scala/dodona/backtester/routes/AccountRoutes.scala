package dodona.backtester.routes

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success}

import _root_.dodona.backtester.actors.{MainSystem, Wallet}
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem, Scheduler}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import io.circe.syntax._
import _root_.dodona.backtester.models.account.OrderModel

object AccountRoutes {
  def apply()(implicit
      system: ActorSystem[MainSystem.Protocol]
  ): AccountRoutes = {
    implicit val timeout: Timeout = 10.seconds
    implicit val ec = system.executionContext

    val wallet = Await
      .result(
        system.ask(MainSystem.GetWalletActor(_)),
        10.seconds
      )
      .asInstanceOf[MainSystem.WalletActor]
    val orderModel = OrderModel()

    new AccountRoutes(wallet.actor, orderModel)
  }
}

class AccountRoutes(walletRef: ActorRef[Wallet.Protocol], orderModel: OrderModel)(implicit
    ec: ExecutionContext,
    scheduler: Scheduler,
    timeout: Timeout
) {
  lazy val apiRoutes: Route = {
    pathPrefix("account") {
      path("balance") {
        parameters("symbol") { symbol =>
          get {
            onComplete(walletRef.ask(ref => Wallet.GetBalance(symbol, ref))) {
              case Failure(exception) => complete(exception)
              case Success(bl)        => complete(bl.value.asJson.toString())
            }
          }
        }
      }
      path("order") {
        get {
          parameters("symbol", "quantity", "side") { (symbol, quantity, side) =>
            onComplete(orderModel.placeOrder(symbol, quantity.toDouble, side)) {
              case Failure(exception) => complete(exception)
              case Success(value) => complete(value)
            }
          }
        }
      }
    }
  }
}
