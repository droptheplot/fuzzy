import com.typesafe.config.ConfigFactory

lazy val conf = ConfigFactory.parseFile(new File("src/main/resources/application.conf"))

name := "Fuzzy"

version := "0.1"

scalaVersion := "2.12.8"

scalacOptions += "-Ypartial-unification"

libraryDependencies += "com.typesafe.akka" %% "akka-http"   % "10.1.5"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.12"
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.5"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"

libraryDependencies += "org.typelevel" %% "cats-core" % "1.5.0"

libraryDependencies += "commons-net" % "commons-net" % "3.6"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"
libraryDependencies += "org.scalamock" % "scalamock_2.12" % "4.1.0"

libraryDependencies += "org.tpolecat" %% "doobie-core" % "0.6.0"
libraryDependencies += "org.tpolecat" %% "doobie-postgres" % "0.6.0"
libraryDependencies += "org.tpolecat" %% "doobie-specs2" % "0.6.0"

libraryDependencies += "com.github.pureconfig" %% "pureconfig" % "0.10.1"

libraryDependencies += "com.chuusai" %% "shapeless" % "2.3.3"

resolvers += "jitpack" at "https://jitpack.io"
resolvers += "ivy2" at "file://"+Path.userHome.absolutePath+"/.ivy2/local"

libraryDependencies += "com.github.droptheplot" % "scala2html" % "master-SNAPSHOT"

enablePlugins(FlywayPlugin)

flywayLocations += conf.getString("migrations")

flywayUrl := conf.getString("default.jdbc.url")
flywayUser := conf.getString("default.jdbc.user")
flywayPassword := conf.getString("default.jdbc.pass")

flywayUrl in Test := conf.getString("test.jdbc.url")
flywayUser in Test := conf.getString("test.jdbc.user")
flywayPassword in Test := conf.getString("test.jdbc.pass")
