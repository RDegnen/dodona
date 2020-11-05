package dodona.strategies.meanreversion

import java.time.{ZoneId, ZonedDateTime}
import java.{util => ju}

import scala.util.{Failure, Success}

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.ws.Message
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Keep, Sink, Source}
import dodona.constants.BinanceConstants.WS_RAW_STREAM_BASE_URL
import dodona.constants.RequestTypes
import dodona.domain.binance.BinanceWsMessage
import dodona.domain.binance.market.{Candlestick, KlineCandlestickInterval}
import dodona.domain.dodona.http.CandlestickParams
import dodona.http.IHttpClient
import dodona.http.mappers.DodonaEnpoints
import dodona.json.binance.Decoders._
import dodona.websocket.IWebSocketClient
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
        //openSocketConnection()
        println(series.getBarData().size())
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
