package dodona.lib.http

sealed trait HttpEndpoint

case object CANDLESTICKS extends HttpEndpoint
case object WEBSOCKET_TOKEN extends HttpEndpoint