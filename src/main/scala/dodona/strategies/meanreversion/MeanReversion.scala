package dodona.strategies.meanreversion

import dodona.http.IHttpClient
import dodona.websocket.IWebSocketClient
import dodona.http.mappers.DodonaEnpoints
import dodona.constants.RequestTypes
import akka.http.scaladsl.model.HttpMethods
import dodona.json.binance.Decoders._
import dodona.domain.dodona.http.CandlestickParams
import scala.util.Success
import scala.util.Failure
import akka.actor.ActorSystem
import dodona.domain.binance.market.Candlestick
import org.ta4j.core.BaseBarSeriesBuilder
import org.ta4j.core.indicators.EMAIndicator
import java.time.ZonedDateTime
import java.{util => ju}
import java.time.ZoneId
import org.ta4j.core.indicators.helpers.ClosePriceIndicator
import org.ta4j.core.indicators.RSIIndicator
import org.ta4j.core.BaseBarSeries
import akka.stream.scaladsl.Source
import dodona.domain.binance.BinanceWsMessage
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Keep
import akka.http.scaladsl.model.ws.Message
import dodona.constants.BinanceConstants.WS_RAW_STREAM_BASE_URL
import io.circe.parser.decode
import dodona.domain.binance.market.KlineCandlestickInterval

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
        // println(series.getBarData().size())
        // println(rsi.getValue(499))
      }
      case Failure(exception) => println(exception)
    }
  }

  def addBarToSeries(candlestick: Candlestick): Unit = {
    println("adding bar")
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
    println("Incoming message", message)
    decode[KlineCandlestickInterval](message.asTextMessage.getStrictText) match {
      case Right(value) => {
        val kc = value.k
        val candlestick = Candlestick(kc.t, kc.o, kc.h, kc.l, kc.c, kc.v, kc.T)
        addBarToSeries(candlestick)
        calculateIndicators()
      }
      case Left(err) => println(err)
    }
  }

  def openSocketConnection(): Unit = {
    import dodona.json.binance.Encoders._
    val wsPair = pair.toLowerCase()
    val (ref, publisher) = Source
      .actorRef[BinanceWsMessage](
        bufferSize = 100,
        overflowStrategy = OverflowStrategy.dropBuffer
      )
      .toMat(Sink.asPublisher(fanout = false))(Keep.both)
      .run()

    val source = Source.fromPublisher(publisher)
    val sink = Sink.foreach[Message](onMessage)

    val (connected, closed) = websocketClient.openSocket[BinanceWsMessage](
      s"$WS_RAW_STREAM_BASE_URL/$wsPair@kline_$interval",
      source,
      sink
    )
    connected.onComplete {
      case Success(_) => {
        println("binance socket open")
      }
      case Failure(exception) => println(exception)
    }
  }
}
