package dodona.websocket

import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.model.ws.TextMessage
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Keep
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.WebSocketRequest
import scala.concurrent.Promise
import dodona.domain.binance.WebSocketMessage
import dodona.json.binance.Encoders.WebSocketMessageEncoder
import io.circe.syntax._
import scala.concurrent.Future
import akka.Done
import akka.http.scaladsl.model.StatusCodes

trait IWebSocketClient {}

class WebSocketClient {
  def openSocketWithMessage(
      url: String,
      message: WebSocketMessage,
      sink: Sink[Message, Future[Done]]
  ): Unit = {
    implicit val system = ActorSystem()
    implicit val executionContext = system.dispatcher

    val flow: Flow[Message, Message, Promise[Option[Message]]] =
      Flow.fromSinkAndSourceMat(
        sink,
        Source
          .single(TextMessage(message.asJson.toString()))
          .concatMat(Source.maybe[Message])(Keep.right)
      )(Keep.right)

    val (upgradeResponse, promise) =
      Http().singleWebSocketRequest(WebSocketRequest(url), flow)

    val connected = upgradeResponse.map { upgrade =>
      if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
        Done
      } else {
        throw new RuntimeException(s"Connection to $url failed: ${upgrade.response.status}")
      }
    }
  }
  // THIS SHIT WORKS FIRST TRY WOOO. Refactor to be more DRY
  def openSocket(url: String, sink: Sink[Message, Future[Done]]): Unit = {
    implicit val system = ActorSystem()
    implicit val executionContext = system.dispatcher

    val flow: Flow[Message, Message, Promise[Option[Message]]] =
      Flow.fromSinkAndSourceMat(
        sink,
        Source
          .empty
          .concatMat(Source.maybe[Message])(Keep.right)
      )(Keep.right)

    val (upgradeResponse, promise) =
      Http().singleWebSocketRequest(WebSocketRequest(url), flow)

    val connected = upgradeResponse.map { upgrade =>
      if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
        Done
      } else {
        throw new RuntimeException(s"Connection to $url failed: ${upgrade.response.status}")
      }
    }
  }
}

// How to use openSocketWithMessage
// val ws = new WebSocketClient()
//   val printSink = Sink.foreach[Message](println)
//   val socket = ws.openSocketWithMessage(
//     WS_RAW_STREAM_BASE_URL,
//     WebSocketMessage("SUBSCRIBE", List("vetusd@kline_1m"), 1),
//     printSink
//   )