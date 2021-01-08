package dodona.strategies.meanreversion

import java.time.{ZoneId, ZonedDateTime}
import java.{util => ju}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.Message
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Keep, Sink, Source}
import dodona.constants.DodonaConstants.BACKTESTER_WS_URL
import dodona.lib.domain.dodona.market.{Candlestick, Trade}
import dodona.lib.websocket.IWebSocketClient
import dodona.strategies.CandlestickBuilder
import io.circe.parser.decode
import org.ta4j.core.indicators.EMAIndicator
import org.ta4j.core.indicators.helpers.ClosePriceIndicator
import org.ta4j.core.trading.rules.{CrossedDownIndicatorRule, CrossedUpIndicatorRule}
import org.ta4j.core.{BaseBarSeries, BaseStrategy}

class MeanReversion(
    // val httpClient: IHttpClient,
    val websocketClient: IWebSocketClient,
    val pair: String,
    val interval: Int
)(implicit val system: ActorSystem, ec: ExecutionContext) {
  private var series: BaseBarSeries = _
  private var strategy: BaseStrategy = _
  private val candlestickBuilder = new CandlestickBuilder(interval)
  // For testing
  private var entered = false
  private var exited = true

  def run(): Unit = {
    // val candlesticks = httpClient.request[List[Candlestick]](
    //   RequestTypes.PUBLIC,
    //   HttpMethods.GET,
    //   DodonaEnpoints.CANDLESTICKS,
    //   CandlestickParams(pair, s"${interval}m")
    // )

    // candlesticks.onComplete {
    //   case Success(candles) => {
    //     series =
    //       new BaseBarSeriesBuilder().withMaxBarCount(500).withName(pair).build()
    //     candles.foreach(addBarToSeries)
    //     setUpStrategy()
    //     openSocketConnection()
    //     println(series.getBarData().size())
    //   }
    //   case Failure(exception) => println(exception)
    // }
  }

  private def setUpStrategy(): Unit = {
    val closePriceIndicator = new ClosePriceIndicator(series)
    val shortEma = new EMAIndicator(closePriceIndicator, 7)
    val longEma = new EMAIndicator(closePriceIndicator, 25)
    val entryRule = new CrossedUpIndicatorRule(shortEma, longEma)
    val exitRule = new CrossedDownIndicatorRule(shortEma, longEma)
    strategy = new BaseStrategy("Cross", entryRule, exitRule)
  }

  private def addBarToSeries(candlestick: Candlestick): Unit = {
    val instant = new ju.Date(candlestick.closeTime).toInstant()
    val zdt = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
    series.addBar(
      zdt,
      candlestick.open,
      candlestick.high,
      candlestick.low,
      candlestick.close
    )
  }

  private def checkEntryOrExit(): Unit = {
    val endIndex = series.getEndIndex()
    val lastBar = series.getBar(endIndex)
    if (strategy.shouldEnter(endIndex)) {
      if (!entered) {
        if (exited) {
          println(s"Entering at ${lastBar.getClosePrice()} - ${lastBar.getEndTime()}")
          entered = true
          exited = false
        }
      }
    } else if (strategy.shouldExit(endIndex)) {
      if (!exited) {
        if (entered) {
          println(s"Exiting at ${lastBar.getClosePrice()} - ${lastBar.getEndTime()}")
          entered = false
          exited = true
        }
      }
    }
  }

  private def onMessage(message: Message): Unit = {
    decode[Trade](message.asTextMessage.getStrictText) match {
      case Right(trade) => {
        candlestickBuilder.addTrade(trade) match {
          case Some(c) => {
            addBarToSeries(c)
            checkEntryOrExit()
          }
          case None => {
            series.addPrice(trade.price)
            checkEntryOrExit()
          }
        }
      }
      case Left(err) => println(err)
    }
  }

  private def openSocketConnection(): Unit = {
    val wsPair = pair.toLowerCase()
    val (ref, publisher) = Source
      .actorRef[Trade](
        bufferSize = 100,
        overflowStrategy = OverflowStrategy.dropBuffer
      )
      .toMat(Sink.asPublisher(fanout = false))(Keep.both)
      .run()

    val source = Source.fromPublisher(publisher)
    val sink = Sink.foreach[Message](onMessage)

    val (connected, closed) = websocketClient.openSocket[Trade](
      s"$BACKTESTER_WS_URL/trade?symbol=${pair}&timeToBegin=1569222899999",
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
