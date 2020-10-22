package dodona.backtester

import com.typesafe.config.ConfigFactory
import dodona.backtester.models.CandlestickModel
import akka.actor.ActorSystem

object BacktesterConfig {
  val conf = ConfigFactory.load()
}

object Backtester extends App {
  implicit val system = ActorSystem()
  implicit val executionContext = system.dispatcher

  val candlestickModel = new CandlestickModel()
  val candlesticks = candlestickModel.getCandlesticks()(executionContext)
}
