package dodona

import dodona.http.HttpClient
import akka.http.scaladsl.model.HttpMethods
import dodona.constants.DodonaConstants.API_BASE_URL
import scala.util.Success
import scala.util.Failure
import akka.actor.ActorSystem
import dodona.json.binance.Decoders._
import dodona.domain.binance.ServerTime

object Dodona extends App {
  implicit val system = ActorSystem()
  implicit val executionContext = system.dispatcher

  val client = new HttpClient(API_BASE_URL)
  val resposne = client.request[ServerTime](HttpMethods.GET, "/api/v3/time")

  resposne.onComplete {
    case Success(value) => println(value)
    case Failure(exception) => println(exception.toString())
  }
}