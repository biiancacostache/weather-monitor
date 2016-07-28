name := "weather-monitor"

version := "1.0"

scalaVersion := "2.11.8"

resolvers ++= Seq(
  "spray repo" at "http://repo.spray.io",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.4",
  "com.typesafe.akka" %% "akka-http-core"  % "2.4.4",
  "com.typesafe.akka" %% "akka-http-spray-json-experimental"  % "2.4.4",
  "com.typesafe.akka" %% "akka-stream"  % "2.4.4",
  "com.typesafe.akka" %% "akka-http-testkit" % "2.4.4",
  "io.spray" %% "spray-json" % "1.3.2",
  "joda-time" % "joda-time" % "2.3",
  "org.joda" % "joda-convert" % "1.6",
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.2.2" % "test"
)
