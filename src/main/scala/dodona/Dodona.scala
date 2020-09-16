package dodona

import dodona.http.HttpClient
import akka.http.scaladsl.model.HttpMethods
import dodona.constants.DodonaConstants.API_BASE_URL
import scala.util.Success
import scala.util.Failure
import akka.actor.ActorSystem
import dodona.json.binance.Decoders._
import dodona.domain.binance._
import com.typesafe.config.ConfigFactory
import akka.http.scaladsl.model.headers.RawHeader

object DodonaConfig {
  val conf = ConfigFactory.load()

  final val BINANCE_US_KEY = conf.getString("Dodona.binanceUsKey")
  final val BINANCE_US_SECRET = conf.getString("Dodona.binanceUsSecrect")
  final val KRAKEN_KEY = conf.getString("Dodona.krakenKey")
  final val KRAKEN_SECRET = conf.getString("Dodona.krakenSecret")
}

object Dodona extends App {
  implicit val system = ActorSystem()
  implicit val executionContext = system.dispatcher

  val client = new HttpClient(API_BASE_URL)
  val resposne = client.request[Account](
    "signed",
    HttpMethods.GET,
    "/api/v3/account",
    Map(),
    headers = Seq(
      RawHeader("X-MBX-APIKEY", DodonaConfig.BINANCE_US_KEY)
    )
  )

  resposne.onComplete {
    case Success(value)     => println(value)
    case Failure(exception) => println(exception.toString())
  }
}
