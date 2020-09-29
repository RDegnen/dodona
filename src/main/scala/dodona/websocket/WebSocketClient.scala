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
import io.circe.syntax._
import scala.concurrent.Future
import akka.Done
import akka.http.scaladsl.model.StatusCodes
import io.circe.Encoder

trait IWebSocketClient {
  def openSocket[WM: Encoder](
      url: String,
      sink: Sink[Message, Future[Done]],
      message: WM = null
  ): Unit
}

class WebSocketClient extends IWebSocketClient {
  def openSocket[WM: Encoder](
      url: String,
      sink: Sink[Message, Future[Done]],
      message: WM = null
  ): Unit = {
    implicit val system = ActorSystem()
    implicit val executionContext = system.dispatcher
    val source =
      if (message != null) Source.single(TextMessage(message.asJson.toString()))
      else Source.empty

    val flow: Flow[Message, Message, Promise[Option[Message]]] =
      Flow.fromSinkAndSourceMat(
        sink,
        source
          .concatMat(Source.maybe[Message])(Keep.right)
      )(Keep.right)

    val (upgradeResponse, promise) =
      Http().singleWebSocketRequest(WebSocketRequest(url), flow)

    val connected = upgradeResponse.map { upgrade =>
      if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
        Done
      } else {
        throw new RuntimeException(
          s"Connection to $url failed: ${upgrade.response.status}"
        )
      }
    }
  }
}
