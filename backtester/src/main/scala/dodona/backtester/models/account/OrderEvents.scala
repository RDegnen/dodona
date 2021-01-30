package dodona.backtester.models.account

import dodona.backtester.lib.domain.OrderFill
import akka.stream.scaladsl.Flow
import akka.http.scaladsl.model.ws.Message
import akka.NotUsed
import akka.stream.scaladsl.Source
import akka.stream.OverflowStrategy
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import akka.http.scaladsl.model.ws.TextMessage
import io.circe.syntax._
import akka.stream.scaladsl.Keep
import akka.stream.scaladsl.BroadcastHub

trait IOrderEvents {
  def push(event: OrderFill): Unit

  def subscribe(): Flow[Message, Message, NotUsed]
}

class OrderEvents(implicit val materializer: Materializer) extends IOrderEvents {
  private val (queue, pub) = Source
    .queue[OrderFill](50, OverflowStrategy.backpressure)
    .toMat(BroadcastHub.sink)(Keep.both)
    .run()

  def push(event: OrderFill): Unit = {
    queue.offer(event)
  }

  def subscribe(): Flow[Message, Message, NotUsed] = {
    val outgoingMessages = pub.map(m => TextMessage(m.asJson.toString()))
    Flow.fromSinkAndSource(Sink.ignore, outgoingMessages)
  }
}
