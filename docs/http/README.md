# HTTP Docs
#### Make a basic request
```
val client = new HttpClient(<exchange>, API_BASE_URL)
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
val client = new HttpClient(Exchanges.KRAKEN, KRAKEN_API_BASE_URL)
val resposne = client.request[KrakenResponse[KrakenServerTime]](
  RequestTypes.SIGNED,
  HttpMethods.POST,
  "/0/private/Balance",
  Map(),
  headers = Seq(
    RawHeader("API-Key", DodonaConfig.KRAKEN_KEY)
  )
)
```
#### API key headers
Binance `RawHeader("X-MBX-APIKEY", DodonaConfig.BINANCE_US_KEY)`

Kraken ` RawHeader("API-Key", DodonaConfig.KRAKEN_KEY)`