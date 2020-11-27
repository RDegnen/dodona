package dodona.backtester

import scala.io.StdIn

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.config.ConfigFactory
import dodona.backtester.actors.MainSystem
import dodona.backtester.routes.{AccountRoutes, CandlestickRoutes, TradeRoutes}

object BacktesterConfig {
  val conf = ConfigFactory.load()

  final val PORT = conf.getInt("DodonaBacktester.port")
}

object Backtester extends App {
  implicit val system = ActorSystem(MainSystem(), "main")
  implicit val ec = system.executionContext

  val candlestickRoutes = new CandlestickRoutes()
  val tradeRoutes = new TradeRoutes()
  val accountRoutes = new AccountRoutes()
  val apiRoutes: Route = {
    pathPrefix("api") {
      concat(
        candlestickRoutes.apiRoutes,
        accountRoutes.apiRoutes
      ) 
    }
  }
  val webSocketRoutes: Route = {
    pathPrefix("ws") {
      concat(tradeRoutes.webSocketRoutes)
    }
  }
  val routes: Route = {
    concat(apiRoutes, webSocketRoutes)
  }
  
  val binding = Http().newServerAt("localhost", BacktesterConfig.PORT).bind(routes)

  println(s"Server running at port ${BacktesterConfig.PORT}")
  StdIn.readLine()
  binding
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}
