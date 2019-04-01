package com

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.all._
import com.entities.{Config, SearchRequest}
import com.handlers.{IndexHandler, RandomHandler, SearchHandler}
import com.services.WhoisService
import com.services.WhoisService.ServerMap
import doobie.util.transactor.Transactor
import org.slf4j.{Logger, LoggerFactory}
import pureconfig.error.ConfigReaderFailures
import pureconfig.generic.auto._

import scala.concurrent.ExecutionContextExecutor
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
        implicit val domainActor: ActorRef = system.actorOf(Props[com.actors.DomainActor])

        WebServer(_config, _servers)
      case (_, _) => IO(ExitCode.Error)
    }
  }

  object WebServer {
    def apply(config: Config, servers: ServerMap)(implicit system: ActorSystem,
                                                  logger: Logger,
                                                  domainActor: ActorRef,
                                                  db: Transactor.Aux[IO, Unit]): IO[ExitCode] = {
      implicit val materializer: ActorMaterializer = ActorMaterializer()
      implicit val executionContext: ExecutionContextExecutor = system.dispatcher

      val route: Route =
        path("") {
          get {
            extractMatchedPath(IndexHandler(_))
          }
        } ~ path("search") {
          get {
            (parameters('query).as(SearchRequest) & extractMatchedPath)(SearchHandler(_, _, servers))
          }
        } ~ path("random") {
          get {
            extractMatchedPath(RandomHandler(_))
          }
        }

      IO.fromFuture(IO(Http().bindAndHandle(route, config.env.host, config.env.port))).as(ExitCode.Success)
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
  }
}
