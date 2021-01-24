ThisBuild / scalaVersion := "2.13.3"
ThisBuild / organization := "dodona"
ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.4.3"

inThisBuild(
  List(
    scalaVersion := "2.13.3",
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision
  )
)

val AkkaVersion = "2.6.10"
val AkkaHttpVersion = "10.2.0"
val CirceVersion = "0.12.3"
val ScalaTestVersion = "3.2.0"
val SlickVersion = "3.3.3"

lazy val dodona = (project in file("."))
  .settings(
    name := "Dodona",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % ScalaTestVersion % "test",
      "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
      "com.typesafe" % "config" % "1.4.0",
      "commons-codec" % "commons-codec" % "1.15",
      "org.ta4j" % "ta4j-core" % "0.13",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
    ),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-stream",
      "com.typesafe.akka" %% "akka-stream-typed",
      "com.typesafe.akka" %% "akka-actor-typed"
    ).map(_ % AkkaVersion),
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
      "org.scalatest" %% "scalatest" % ScalaTestVersion % "test",
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % "test",
      "org.slf4j" % "slf4j-nop" % "1.6.4",
      "org.xerial" % "sqlite-jdbc" % "3.32.3.2"
    ),
    libraryDependencies ++= Seq(
      "com.typesafe.slick" %% "slick",
      "com.typesafe.slick" %% "slick-hikaricp",
    ).map(_ % SlickVersion),
    scalacOptions += "-Ywarn-unused:imports"
  )