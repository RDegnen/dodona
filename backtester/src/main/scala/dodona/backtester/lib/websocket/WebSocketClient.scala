package dodona.backtester.lib.websocket

import scala.concurrent.Future

import akka.http.scaladsl.model.ws.Message
import akka.stream.scaladsl.{Sink, Source}
import akka.{Done, NotUsed}
import dodona.websocket.IWebSocketClient
import io.circe.Encoder
import akka.actor.ActorSystem
import scala.concurrent.ExecutionContext

class WebSocketClient extends IWebSocketClient {
  def openSocket[WM: Encoder](
      url: String,
      source: Source[WM, NotUsed],
      sink: Sink[Message, Future[Done]]
  )(implicit
      system: ActorSystem,
      ec: ExecutionContext
  ): (Future[Done], Future[Done]) = ???
}
