package dodona.backtester

import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import dodona.backtester.routes.CandlestickRoutes
import scala.io.StdIn

object BacktesterConfig {
  val conf = ConfigFactory.load()

  final val PORT = conf.getInt("DodonaBacktester.port")
}

object Backtester extends App {
  implicit val system = ActorSystem()
  implicit val executionContext = system.dispatcher

  val candlestickRoutes = new CandlestickRoutes()
  val routes: Route = pathPrefix("backtester") {
    concat(candlestickRoutes.routes)
  }
  val binding = Http().newServerAt("localhost", BacktesterConfig.PORT).bind(routes)

  println(s"Server running at port ${BacktesterConfig.PORT}")
  StdIn.readLine()
  binding
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}
