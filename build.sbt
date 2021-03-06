import com.typesafe.config.ConfigFactory

import scala.util.Properties

lazy val conf = ConfigFactory.parseFile(new File("src/main/resources/application.conf"))

name := "Fuzzy"

version := "0.1"

scalaVersion := "2.12.8"

scalacOptions += "-Ypartial-unification"

libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.1.5"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.12"
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.5.22" % Test
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.5"

libraryDependencies += "org.http4s" %% "http4s-blaze-server" % "0.19.0"
libraryDependencies += "org.http4s" %% "http4s-dsl" % "0.19.0"

libraryDependencies +="co.fs2" %% "fs2-core" % "1.0.4"
libraryDependencies +="co.fs2" %% "fs2-io" % "1.0.4"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"

libraryDependencies += "org.typelevel" %% "cats-core" % "1.5.0"

libraryDependencies += "commons-net" % "commons-net" % "3.6"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"
libraryDependencies += "org.scalamock" % "scalamock_2.12" % "4.1.0"

libraryDependencies += "org.tpolecat" %% "doobie-core" % "0.6.0"
libraryDependencies += "org.tpolecat" %% "doobie-postgres" % "0.6.0"
libraryDependencies += "org.tpolecat" %% "doobie-specs2" % "0.6.0"
libraryDependencies += "org.tpolecat" %% "doobie-scalatest" % "0.6.0"

libraryDependencies += "io.circe" %% "circe-core" % "0.10.0"
libraryDependencies += "io.circe" %% "circe-generic" % "0.10.0"
libraryDependencies += "io.circe" %% "circe-parser" % "0.10.0"

libraryDependencies += "com.github.pureconfig" %% "pureconfig" % "0.10.1"

libraryDependencies += "com.chuusai" %% "shapeless" % "2.3.3"

libraryDependencies += "org.flywaydb" % "flyway-core" % "5.2.4"

resolvers += "jitpack" at "https://jitpack.io"
resolvers += "ivy2" at "file://"+Path.userHome.absolutePath+"/.ivy2/local"

libraryDependencies += "com.github.droptheplot" % "scala2html" % "1.0.0"

enablePlugins(FlywayPlugin, JavaAppPackaging)

flywayLocations += conf.getString(s"$env.jdbc.migrations")

val env: String = Properties.envOrElse("FUZZY_ENV", "development")

flywayUrl := conf.getString(s"$env.jdbc.url")
flywayUser := conf.getString(s"$env.jdbc.user")
flywayPassword := conf.getString(s"$env.jdbc.pass")

flywayUrl in Test := conf.getString("test.jdbc.url")
flywayUser in Test := conf.getString("test.jdbc.user")
flywayPassword in Test := conf.getString("test.jdbc.pass")

initialCommands in console :=
  """
    |import fuzzy.Main
    |import fuzzy.repositories._
    |import fuzzy.entities.Config
    |import pureconfig.error.ConfigReaderFailures
    |import pureconfig.generic.auto._
    |import doobie.implicits._
    |
    |var config = pureconfig.loadConfig[Config]
    |var db = Main.Database(config.right.get)
    |""".stripMargin
