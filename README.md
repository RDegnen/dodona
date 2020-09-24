# Dodona
## Documentation
A place to write down things so I don't forget them.
### HTTP
Make a basic request
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
Make to make a signed request just do
```
RequestTypes.SIGNED
```
### WebSocket
#### Open a new socket with a message
```
val ws = new WebSocketClient()
val printSink = Sink.foreach[Message](println)
val socket = ws.openSocketWithMessage(
  WS_RAW_STREAM_BASE_URL,
  WebSocketMessage("SUBSCRIBE", List("vetusd@kline_1m"), 1),
  printSink
)
```
#### Open a socket using a listen key
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
    ws.openSocketWithMessage(s"$WS_RAW_STREAM_BASE_URL/$key", printSink)
  }
  case Failure(exception) => println(exception.toString())
}
```