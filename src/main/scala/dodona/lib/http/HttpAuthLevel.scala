package dodona.lib.http

sealed trait HttpAuthLevel

case object PUBLIC extends HttpAuthLevel
case object SIGNED extends HttpAuthLevel
