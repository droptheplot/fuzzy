package com.entities

final case class Config(migrations: String, default: Config.Default, test: Config.Test)

object Config {
  trait Env {
    def host: String
    def port: Int
    def jdbc: JDBC
  }

  case class JDBC(driver: String, url: String, user: String, pass: String)

  case class Default(host: String, port: Int, jdbc: JDBC) extends Env
  case class Test(host: String, port: Int, jdbc: JDBC) extends Env
}
