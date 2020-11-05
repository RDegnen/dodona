package dodona.lib.websocket

import scala.concurrent.{ExecutionContext, Future}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.{Done, NotUsed}
import io.circe.Encoder
import io.circe.syntax._

trait IWebSocketClient {
  def openSocket[WM: Encoder](
      url: String,
      source: Source[WM, NotUsed],
      sink: Sink[Message, Future[Done]]
  )(implicit
      system: ActorSystem,
      ec: ExecutionContext
  ): (Future[Done], Future[Done])
}

class WebSocketClient extends IWebSocketClient {
  def openSocket[WM: Encoder](
      url: String,
      source: Source[WM, NotUsed],
      sink: Sink[Message, Future[Done]]
  )(implicit
      system: ActorSystem,
      ec: ExecutionContext
  ): (Future[Done], Future[Done]) = {
    val messageSource =
      source.map(message => TextMessage(message.asJson.toString()))
    val flow = Flow.fromSinkAndSourceMat(sink, messageSource)(Keep.left)

    val (upgradeResponse, closed) =
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

    (connected, closed)
  }
}
