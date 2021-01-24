package dodona.backtester.routes

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success}

import _root_.dodona.backtester.actors.{MainSystem, Wallet}
import _root_.dodona.backtester.models.account.OrderModel
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem, Scheduler}
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import io.circe.syntax._

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

class AccountRoutes(
    walletRef: ActorRef[Wallet.Protocol],
    orderModel: OrderModel
)(implicit
    ec: ExecutionContext,
    scheduler: Scheduler,
    timeout: Timeout
) {
  lazy val apiRoutes: Route = {
    pathPrefix("account") {
      concat(
        path("balance") {
          get {
            onComplete(walletRef.ask(ref => Wallet.GetBalance(ref))) {
              case Failure(exception) => complete(exception)
              case Success(value)     => complete(value.balance.asJson.toString)
            }
          }
        },
        path("order") {
          post {
            parameters("pair", "orderType", "quantity", "side") {
              (pair, orderType, quantity, side) =>
                onComplete(
                  // Order type (MARKET, LIMIT, ect..) not implemented
                  orderModel.placeOrder(pair, quantity.toDouble, side)
                ) {
                  case Failure(exception) =>
                    val response = HttpResponse(
                      StatusCodes.InternalServerError,
                      entity = exception.toString
                    )
                    complete(response)
                  case Success(value) => value match {
                    case Left(value) =>
                      val response = HttpResponse(
                        StatusCodes.BadRequest,
                        entity = value
                      )
                      complete(response)
                    case Right(value) => complete(value.asJson.toString)
                  }
                }
            }
          }
        }
      )
    }
  }
}
