package dodona.lib.http

final case class HttpAuthLevel(value: String)

object HttpAuthLevels {
  final val PUBLIC = HttpAuthLevel("PUBLIC")
  final val SIGNED = HttpAuthLevel("SIGNED")
}
