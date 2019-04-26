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
        val system: ActorSystem = ActorSystem()
        val logger: Logger = LoggerFactory.getLogger(Main.getClass)
        val db: Transactor.Aux[IO, _] = Database(config)
        val domainActor: ActorRef = system.actorOf(Props[DomainActor])

        Database.migrate(config)

        WebServer(config)(serverMap, system, logger, domainActor, db)
      case (_, _) => IO(ExitCode.Error)
    }
  }
}
