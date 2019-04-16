package fuzzy.repositories

import cats.effect.{ContextShift, IO}
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import fuzzy.entities.Config
import org.scalatest.FunSuite
import pureconfig.generic.auto._

import scala.concurrent.ExecutionContext

trait RepositoryTest { this: FunSuite =>
  val config: Config = pureconfig.loadConfig[Config] match {
    case Right(_config) => _config
    case Left(err)      => fail(err.toString)
  }

  val ec: ExecutionContext = ExecutionContext.global
  implicit val cs: ContextShift[IO] = IO.contextShift(ec)

  val transactor: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    config.test.jdbc.driver,
    config.test.jdbc.url,
    config.test.jdbc.user,
    config.test.jdbc.pass,
  )
}
