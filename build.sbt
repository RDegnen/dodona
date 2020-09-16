ThisBuild / scalaVersion := "2.13.3"
ThisBuild / organization := "dodona"

inThisBuild(
  List(
    scalaVersion := "2.13.3",
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision
  )
)

val AkkaVersion = "2.6.8"
val AkkaHttpVersion = "10.2.0"
val CirceVersion = "0.12.3"

lazy val dodona = (project in file("."))
  .settings(
    name := "Dodona",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.0" % "test",
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
      "com.typesafe" % "config" % "1.4.0",
      "commons-codec" % "commons-codec" % "1.15"
    ),
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % CirceVersion),
    resolvers += "liferaypublic" at "https://repository.liferay.com/nexus/content/repositories/public/",
    scalacOptions += "-Ywarn-unused:imports",
  )
