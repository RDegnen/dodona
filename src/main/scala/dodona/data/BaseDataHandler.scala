package dodona.data

import java.time.{ZoneId, ZonedDateTime}
import java.{util => ju}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.ws.Message
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Keep, Sink, Source}
import dodona.MainSystem
import dodona.lib.domain.dodona.market.Candlestick
import dodona.lib.http.BaseHttpClient
import dodona.lib.websocket.IWebSocketClient
import io.circe.Encoder
import org.ta4j.core.{BaseBarSeries, BaseBarSeriesBuilder}

abstract class BaseDataHandler(pair: String, interval: Int)(implicit val system: ActorSystem[MainSystem.Protocol], ec: ExecutionContext) {
  protected val candlestickBuilder = new CandlestickBuilder(interval)
  protected val httpClient: BaseHttpClient
  protected val webSocketClient: IWebSocketClient
  val series: BaseBarSeries = new BaseBarSeriesBuilder().withMaxBarCount(500).withName(pair).build()

  def initialize(): Unit
  
  protected def addBarToSeries(bar: Candlestick): Unit = {
    val instant = new ju.Date(bar.closeTime).toInstant()
    val zdt = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
    series.addBar(
      zdt,
      bar.open,
      bar.high,
      bar.low,
      bar.close
    )
  }

  protected def openSocket[TickType: Encoder](url: String, onMessage: Message => Unit): Unit = {
    val pairLower = pair.toLowerCase
    val (ref, publisher) = Source
      .actorRef[TickType](
        bufferSize = 100,
        overflowStrategy = OverflowStrategy.dropBuffer
      )
      .toMat(Sink.asPublisher(fanout = false))(Keep.both)
      .run

    val source = Source.fromPublisher(publisher)
    val sink = Sink.foreach[Message](onMessage)

    val (connected, closed) = webSocketClient.openSocket[TickType](
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
