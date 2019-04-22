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
    val config: Either[ConfigReaderFailures, Config] = pureconfig.loadConfig[Config]
    val servers: Try[ServerMap] = WhoisService.loadServers()

    implicit val system: ActorSystem = ActorSystem()
    implicit val logger: Logger = LoggerFactory.getLogger(Main.getClass)

    (config, servers) match {
      case (Right(_config), Success(_servers)) =>
        implicit val db: Transactor.Aux[IO, Unit] = Database(_config)
        implicit val domainActor: ActorRef = system.actorOf(Props[DomainActor])

        Database.migrate(_config)

        WebServer(_config, _servers)
      case (_, _) => IO(ExitCode.Error)
    }
  }
}
