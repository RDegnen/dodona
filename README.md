# Dodona
## Documentation
A place to write down things so I don't forget them.
### HTTP
#### Make a basic request
```
val client = new HttpClient(API_BASE_URL)
val resposne = client.request[Account](
  RequestTypes.PUBLIC,
  HttpMethods.GET,
  "/api/v3/account",
  Map(),
  headers = Seq(
    RawHeader("X-MBX-APIKEY", DodonaConfig.BINANCE_US_KEY)
  )
)

resposne.onComplete {
  case Success(value)     => println(value)
  case Failure(exception) => println(exception.toString())
}
```
#### To make a signed request just do
```
RequestTypes.SIGNED
```
Kraken signed example
```
val client = new HttpClient(KRAKEN_API_BASE_URL)
val resposne = client.request[KrakenResponse[KrakenServerTime]](
  Exchanges.KRAKEN,
  RequestTypes.SIGNED,
  HttpMethods.POST,
  "/0/private/Balance",
  Map(),
  headers = Seq(
    RawHeader("API-Key", DodonaConfig.KRAKEN_KEY)
  )
)
```
#### API keys
Binance `RawHeader("X-MBX-APIKEY", DodonaConfig.BINANCE_US_KEY)`
### WebSocket
#### Open a new socket with a message
```
val ws = new WebSocketClient()
val printSink = Sink.foreach[Message](println)
val socket = ws.openSocket[WebSocketMessage](
  WS_RAW_STREAM_BASE_URL,
  WebSocketMessage("SUBSCRIBE", List("vetusd@kline_1m"), 1),
  printSink
)
```
#### Open a Binance socket using a listen key
```
val ws = new WebSocketClient()
val client = new HttpClient(API_BASE_URL)
val getListenKey = client.request[ListenKey](
  RequestTypes.PUBLIC,
  HttpMethods.POST,
  "/api/v3/userDataStream",
  Map(),
  headers = Seq(
    RawHeader("X-MBX-APIKEY", DodonaConfig.BINANCE_US_KEY)
  )
)

val printSink = Sink.foreach[Message](println)
getListenKey.onComplete {
  case Success(value)     => {
    val key = value.listenKey
    ws.openSocket[WebSocketMessage](s"$WS_RAW_STREAM_BASE_URL/$key", printSink)
  }
  case Failure(exception) => println(exception.toString())
}
```
#### Open a Kraken socket using a token
```
val ws = new WebSocketClient()
val client = new HttpClient(KRAKEN_API_BASE_URL)
val resposne = client.request[KrakenResponse[WebSocketToken]](
  Exchanges.KRAKEN,
  RequestTypes.SIGNED,
  HttpMethods.POST,
  "/0/private/GetWebSocketsToken",
  Map(),
  headers = Seq(
    RawHeader("API-Key", DodonaConfig.KRAKEN_KEY)
  )
)

val printSink = Sink.foreach[Message](println)
resposne.onComplete {
  case Success(value)     => {
    val subscription = WebSocketSubscription("ownTrades", value.result.token)
    val message = KrakenWebSocketMessage("subscribe", subscription)

    ws.openSocket[KrakenWebSocketMessage](KRAKEN_PRIVATE_WS_URL, printSink,message)
  }
  case Failure(exception) => println(exception.toString())
}
```