package dodona.backtester

import scala.io.StdIn

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.config.ConfigFactory
import dodona.backtester.routes.{CandlestickRoutes, SpreadRoutes, TradeRoutes}

object BacktesterConfig {
  val conf = ConfigFactory.load()

  final val PORT = conf.getInt("DodonaBacktester.port")
}

object Backtester extends App {
  implicit val system = ActorSystem()
  implicit val executionContext = system.dispatcher

  val candlestickRoutes = new CandlestickRoutes()
  val spreadRoutes = new SpreadRoutes()
  val tradeRoutes = new TradeRoutes()
  val routes: Route = {
    concat(
      candlestickRoutes.routes,
      spreadRoutes.routes,
      tradeRoutes.routes
    )
  }
  
  val binding = Http().newServerAt("localhost", BacktesterConfig.PORT).bind(routes)

  println(s"Server running at port ${BacktesterConfig.PORT}")
  StdIn.readLine()
  binding
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}
