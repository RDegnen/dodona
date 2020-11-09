package dodona.strategies.meanreversion

import java.time.{ZoneId, ZonedDateTime}
import java.{util => ju}

import scala.util.{Failure, Success}

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.ws.Message
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Keep, Sink, Source}
import dodona.constants.DodonaConstants.BACKTESTER_WS_URL
import dodona.constants.RequestTypes
import dodona.lib.domain.binance.market.Candlestick
import dodona.lib.domain.dodona.http.CandlestickParams
import dodona.lib.domain.dodona.market.Spread
import dodona.lib.http.IHttpClient
import dodona.lib.http.mappers.DodonaEnpoints
import dodona.lib.json.binance.Decoders._
import dodona.lib.websocket.IWebSocketClient
import io.circe.parser.decode
import org.ta4j.core.indicators.EMAIndicator
import org.ta4j.core.indicators.helpers.ClosePriceIndicator
import org.ta4j.core.{BaseBarSeries, BaseBarSeriesBuilder}

class MeanReversion(
    val httpClient: IHttpClient,
    val websocketClient: IWebSocketClient,
    val pair: String
) {
  implicit val system = ActorSystem()
  implicit val executionContext = system.dispatcher
  private var series: BaseBarSeries = _
  private val interval = "15m"

  def run(): Unit = {
    val candlesticks = httpClient.request[List[Candlestick]](
      RequestTypes.PUBLIC,
      HttpMethods.GET,
      DodonaEnpoints.CANDLESTICKS,
      CandlestickParams(pair, interval)
    )

    candlesticks.onComplete {
      case Success(candles) => {
        series =
          new BaseBarSeriesBuilder().withMaxBarCount(500).withName(pair).build()
        candles.foreach(addBarToSeries) 
        openSocketConnection()
        println(series.getBarData().size())
      }
      case Failure(exception) => println(exception)
    }
  }

  def addBarToSeries(candlestick: Candlestick): Unit = {
    val instant = new ju.Date(candlestick.closeTime).toInstant()
    val zdt = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
    series.addBar(
      zdt,
      candlestick.open,
      candlestick.high,
      candlestick.low,
      candlestick.close,
      candlestick.volume
    )
  }

  def calculateIndicators(): Unit = {
    val closePriceIndicator = new ClosePriceIndicator(series)
    val shortEma = new EMAIndicator(closePriceIndicator, 14)
    val longEma = new EMAIndicator(closePriceIndicator, 50)
    println(shortEma.getValue(499), longEma.getValue(499))
  }

  def onMessage(message: Message): Unit = {
    decode[Spread](message.asTextMessage.getStrictText) match {
      case Right(spread) => {
        println(spread)
      }
      case Left(err) => println(err)
    }
  }

  def openSocketConnection(): Unit = {
    val wsPair = pair.toLowerCase()
    val (ref, publisher) = Source
      .actorRef[Spread](
        bufferSize = 100,
        overflowStrategy = OverflowStrategy.dropBuffer
      )
      .toMat(Sink.asPublisher(fanout = false))(Keep.both)
      .run()

    val source = Source.fromPublisher(publisher)
    val sink = Sink.foreach[Message](onMessage)

    val (connected, closed) = websocketClient.openSocket[Spread](
      s"$BACKTESTER_WS_URL/spread",
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
