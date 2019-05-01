package fuzzy

import akka.actor.{ActorRef, ActorSystem, Props}
import cats.effect.{ExitCode, IO, IOApp}
import doobie.util.transactor.Transactor
import fuzzy.actors.DomainActor
import fuzzy.entities._
import fuzzy.services.WhoisService
import org.slf4j.{Logger, LoggerFactory}
import pureconfig.error.ConfigReaderFailures
import pureconfig.generic.auto._

import scala.util.{Success, Try}

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    val maybeConfig: Either[ConfigReaderFailures, Config] = pureconfig.loadConfig[Config]
    val maybeServerMap: Try[ServerMap] = WhoisService.loadServers()

    (maybeConfig, maybeServerMap) match {
      case (Right(config), Success(serverMap)) =>
        val db: Database[IO] = new Database[IO]()
        val system: ActorSystem = ActorSystem()
        val logger: Logger = LoggerFactory.getLogger(Main.getClass)
        val xa: Transactor.Aux[IO, _] = db.run(config)
        val domainActor: ActorRef = system.actorOf(Props[DomainActor])

        db.migrate(config)

        new WebServer[IO]()
          .run(config)(serverMap, system, logger, domainActor, xa)
          .compile
          .drain
          .map(_ => ExitCode.Success)

      case (_, _) => IO(ExitCode.Error)
    }
  }
}
