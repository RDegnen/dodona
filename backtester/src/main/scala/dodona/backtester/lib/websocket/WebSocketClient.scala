package dodona.backtester.lib.websocket

import scala.concurrent.Future

import akka.http.scaladsl.model.ws.Message
import akka.stream.scaladsl.{Sink, Source}
import akka.{Done, NotUsed}
import dodona.websocket.IWebSocketClient
import io.circe.Encoder

class WebSocketClient extends IWebSocketClient {
  def openSocket[WM: Encoder](url: String, source: Source[WM,NotUsed], sink: Sink[Message,Future[Done]]): (Future[Done], Future[Done]) = ???
}
