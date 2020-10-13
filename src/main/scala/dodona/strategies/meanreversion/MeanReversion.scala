package dodona.strategies.meanreversion

import dodona.http.IHttpClient
import dodona.websocket.IWebSocketClient
import dodona.constants.RequestTypes
import akka.http.scaladsl.model.HttpMethod
import akka.http.scaladsl.model.HttpMethods

class MeanReversion(val httpClient: IHttpClient, val websocketClient: IWebSocketClient, symbolPair: String) {
  // def start(): Unit = {
  //   httpClient.request(
  //     RequestTypes.PUBLIC,
  //     HttpMethods.GET,
  //     "/api/v3/klines"
  //   )
  // }
}
