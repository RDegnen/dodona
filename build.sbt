ThisBuild / scalaVersion := "2.13.3"
ThisBuild / organization := "dodona"

inThisBuild(
  List(
    scalaVersion := "2.13.3",
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision
  )
)

val AkkaVersion = "2.6.9"
val AkkaHttpVersion = "10.2.0"
val CirceVersion = "0.12.3"
val ScalaTestVersion = "3.2.0"
val SlickVersion = "3.3.3"

lazy val dodona = (project in file("."))
  .settings(
    name := "Dodona",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % ScalaTestVersion % "test",
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
      "com.typesafe" % "config" % "1.4.0",
      "commons-codec" % "commons-codec" % "1.15",
      "org.ta4j" % "ta4j-core" % "0.13"
    ),
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % CirceVersion),
    resolvers += "liferaypublic" at "https://repository.liferay.com/nexus/content/repositories/public/",
    scalacOptions += "-Ywarn-unused:imports",
  )

lazy val dodonaBacktester = (project in file("backtester"))
  .dependsOn(dodona)
  .settings(
    name := "Dodona Backtester",
    libraryDependencies ++= Seq(
      "com.typesafe.slick" %% "slick" % SlickVersion,
      "org.slf4j" % "slf4j-nop" % "1.6.4",
      "com.typesafe.slick" %% "slick-hikaricp" % SlickVersion,
      "org.xerial" % "sqlite-jdbc" % "3.32.3.2"
    ),
    scalacOptions += "-Ywarn-unused:imports",
  )