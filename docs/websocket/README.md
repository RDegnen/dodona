# Websocket Docs
## Big Example
### Private connection
```
  val krakenTest = () => {
    val ws = new WebSocketClient()
    val client = new HttpClient(Exchanges.KRAKEN, KRAKEN_API_BASE_URL)
    val resposne = client.request[KrakenResponse[KrakenWsToken]](
      RequestTypes.SIGNED,
      HttpMethods.POST,
      "/0/private/GetWebSocketsToken",
      Map(),
      headers = Seq(
        RawHeader("API-Key", DodonaConfig.KRAKEN_KEY)
      )
    )

    val (ref, publisher) = Source
      .actorRef[KrakenWsMessage](bufferSize = 100, overflowStrategy = OverflowStrategy.dropBuffer)
      .toMat(Sink.asPublisher(fanout = false))(Keep.both)
      .run()
    
    val source = Source.fromPublisher(publisher)

    val printSink = Sink.foreach[Message](println)
    resposne.onComplete {
      case Success(value)     => {
        val subscription = KrakenWsSubscription("ownTrades", value.result.token)
        val message = KrakenWsMessage("subscribe", subscription)

        val (connected, closed) = ws.openSocket[KrakenWsMessage](KRAKEN_PRIVATE_WS_URL, source, printSink)
        connected.onComplete {
          case Success(_) => {
            println("kraken socket open")
            ref ! message
          }
          case Failure(exception) => println(exception)
        }
      }
      case Failure(exception) => println(exception.toString())
    }
  }
```
```
  val binanceTest = () => {
    import dodona.json.binance.Encoders._

    val ws = new WebSocketClient()
    val client = new HttpClient(Exchanges.BINANCE, API_BASE_URL)
    val getListenKey = client.request[BinanceListenKey](
      RequestTypes.PUBLIC,
      HttpMethods.POST,
      "/api/v3/userDataStream",
      Map(),
      headers = Seq(
        RawHeader("X-MBX-APIKEY", DodonaConfig.BINANCE_US_KEY)
      )
    )

    val (ref, publisher) = Source
      .actorRef[BinanceWsMessage](bufferSize = 100, overflowStrategy = OverflowStrategy.dropBuffer)
      .toMat(Sink.asPublisher(fanout = false))(Keep.both)
      .run()
    
    val source = Source.fromPublisher(publisher)

    val printSink = Sink.foreach[Message](println)
    getListenKey.onComplete {
      case Success(value) => {
        val key = value.listenKey
        val (connected, closed) = ws.openSocket[BinanceWsMessage](s"$WS_RAW_STREAM_BASE_URL/$key", source, printSink)

        connected.onComplete {
          case Success(_) => {
            println("binance socket open")
          }
          case Failure(exception) => println(exception)
        }
      }
    }
  }
```