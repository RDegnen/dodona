package dodona

import akka.actor.typed.ActorSystem
import com.typesafe.config.ConfigFactory
import dodona.data.handlers.BacktesterDataHandler
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import scala.concurrent.duration._
import scala.util.Success
import scala.util.Failure
import dodona.strategies.meanreversion.MeanReversion
import dodona.portfolio.portfolios.BacktesterPortfolio
import dodona.execution.handlers.BacktesterExecutionHandler

object DodonaConfig {
  val conf = ConfigFactory.load()

  final val BINANCE_US_KEY = conf.getString("Dodona.binanceUsKey")
  final val BINANCE_US_SECRET = conf.getString("Dodona.binanceUsSecrect")
  final val KRAKEN_KEY = conf.getString("Dodona.krakenKey")
  final val KRAKEN_SECRET = conf.getString("Dodona.krakenSecret")
}

object Dodona extends App {
  implicit val system = ActorSystem(MainSystem(), "main")
  implicit val ec = system.executionContext
  implicit val timeout: Timeout = 10.seconds

  val pair = "ETHUSD"
  val strategy = new MeanReversion()
  val portfolio = new BacktesterPortfolio("USD")
  val executionHandler = new BacktesterExecutionHandler()
  system.ask(ref => MainSystem.InitEvents(ref, strategy, portfolio, executionHandler)).onComplete {
    case Success(value) => {
      val reply = value.asInstanceOf[MainSystem.EventQueueReply]
      val eq = reply.actor
      val dh = new BacktesterDataHandler(pair, 15, eq)
      portfolio.initialize(dh, eq)
      dh.initialize
      strategy.initialize(dh, eq, pair)
      executionHandler.initialize(eq)
    }
    case Failure(err) => {
      println(err)
      system.terminate()
    }
  }
}