package fuzzy

import cats.effect.{ConcurrentEffect, ContextShift}
import cats.Applicative
import doobie.util.transactor.Transactor
import fuzzy.entities.Config
import org.flywaydb.core.Flyway
import pureconfig.generic.auto._

import scala.util.Try

class Database[F[_]: Applicative: ContextShift: ConcurrentEffect] {
  def run(config: Config): Transactor.Aux[F, _] = {
    Transactor.fromDriverManager[F](
      config.env.jdbc.driver,
      config.env.jdbc.url,
      config.env.jdbc.user,
      config.env.jdbc.pass,
    )
  }

  def migrate(config: Config): Try[Int] = Try {
    Flyway
      .configure()
      .locations(config.env.jdbc.migrations)
      .dataSource(
        config.env.jdbc.url,
        config.env.jdbc.user,
        config.env.jdbc.pass
      )
      .load()
      .migrate()
  }
}
