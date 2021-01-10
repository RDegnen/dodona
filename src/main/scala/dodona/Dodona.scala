package dodona

import akka.actor.typed.ActorSystem
import com.typesafe.config.ConfigFactory
import dodona.data.handlers.BacktesterDataHandler
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import scala.concurrent.duration._
import scala.util.Success
import scala.util.Failure

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

  system.ask(MainSystem.GetEventHandler).onComplete {
    case Success(value) => {
      val reply = value.asInstanceOf[MainSystem.EventHandlerReply]
      val dh = new BacktesterDataHandler("ETHUSD", 15, reply.actor)
      dh.initialize
    }
    case Failure(err) => {
      println(err)
      system.terminate()
    }
  }
}