package fuzzy

import akka.actor.{ActorRef, ActorSystem, Props}
import cats.effect.{ExitCode, IO, IOApp}
import doobie.util.transactor.Transactor
import fuzzy.actors.DomainActor
import fuzzy.entities.{Config, SearchRequest}
import fuzzy.handlers._
import fuzzy.services.WhoisService
import fuzzy.services.WhoisService.ServerMap
import org.flywaydb.core.Flyway
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.server.blaze.BlazeServerBuilder
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

  object WebServer {
    def apply(config: Config, servers: ServerMap)(implicit system: ActorSystem,
                                                  logger: Logger,
                                                  domainActor: ActorRef,
                                                  db: Transactor.Aux[IO, Unit]): IO[ExitCode] = {
      object QueryParamMatcher extends QueryParamDecoderMatcher[String]("query")

      BlazeServerBuilder[IO]
        .bindHttp(config.env.port, config.env.host)
        .withHttpApp(HttpRoutes
          .of[IO] {
            case GET -> Root                                        => IndexHandler()
            case GET -> Root / "search" :? QueryParamMatcher(query) => SearchHandler(SearchRequest(query), servers)
            case GET -> Root / "random"                             => RandomHandler()
          }
          .orNotFound)
        .serve
        .compile
        .drain
        .map(_ => ExitCode.Success)
    }
  }

  object Database {
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
}
