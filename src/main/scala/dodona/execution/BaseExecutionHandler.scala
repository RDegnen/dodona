package dodona.execution

import akka.actor.typed.ActorRef
import dodona.events.EventQueue
import io.circe.Encoder
import akka.http.scaladsl.model.ws.Message
import akka.stream.scaladsl.Source
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Keep
import dodona.lib.websocket.IWebSocketClient
import akka.actor.typed.ActorSystem
import dodona.MainSystem
import scala.concurrent.ExecutionContext
import scala.util.Success
import scala.util.Failure

abstract class BaseExecutionHandler(implicit val system: ActorSystem[MainSystem.Protocol], ec: ExecutionContext) extends IExecutionHandler {
  protected val webSocketClient: IWebSocketClient
  def initialize(eq: ActorRef[EventQueue.Push]): Unit

  def executeOrder(pair: String, orderType: String, quantity: BigDecimal, side: String): Unit

  protected def openSocket[T: Encoder](url: String, onMessage: Message => Unit): Unit = {
    val (ref, publisher) = Source
      .actorRef[T](
        bufferSize = 100,
        overflowStrategy = OverflowStrategy.dropBuffer
      )
      .toMat(Sink.asPublisher(false))(Keep.both)
      .run

    val source = Source.fromPublisher(publisher)
    val sink = Sink.foreach[Message](onMessage)

    val (connected, closed) = webSocketClient.openSocket[T](
      url,
      source,
      sink
    )
    connected.onComplete {
      case Success(_) => {
        println("Socket open")
      }
      case Failure(exception) => println(exception)
    }
  }
}
