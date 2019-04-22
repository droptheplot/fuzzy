package fuzzy

import cats.effect.{ContextShift, IO}
import doobie.util.transactor.Transactor
import fuzzy.entities.Config
import org.flywaydb.core.Flyway
import pureconfig.generic.auto._

import scala.concurrent.ExecutionContext
import scala.util.Try

object Database {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  def apply(config: Config): Transactor.Aux[IO, Unit] = {
    Transactor.fromDriverManager[IO](
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
