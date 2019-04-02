package com.entities

import com.entities.Config._

import scala.util.Properties

final case class Config(development: Development, production: Production, test: Test) {
  def env: Env = Properties.envOrElse("FUZZY_ENV", "development") match {
    case "production"  => production
    case "development" => development
    case "test"        => test
  }
}

object Config {
  trait Env {
    def host: String
    def port: Int
    def jdbc: JDBC
  }

  case class JDBC(driver: String, url: String, user: String, pass: String, migrations: String)

  case class Development(host: String, port: Int, jdbc: JDBC) extends Env
  case class Production(host: String, port: Int, jdbc: JDBC) extends Env
  case class Test(host: String, port: Int, jdbc: JDBC) extends Env
}
