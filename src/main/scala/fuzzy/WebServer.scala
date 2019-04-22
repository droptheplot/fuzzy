package fuzzy

import java.util.concurrent.Executors

import akka.actor.{ActorRef, ActorSystem}
import cats.effect.{ContextShift, ExitCode, IO}
import doobie.util.transactor.Transactor
import fuzzy.entities._
import fuzzy.handlers._
import org.http4s.{HttpRoutes, StaticFile}
import org.http4s.dsl.io._
import org.http4s.server.blaze.BlazeServerBuilder
import org.slf4j.Logger
import pureconfig.generic.auto._

import scala.concurrent.ExecutionContext

object WebServer {
  object QueryParamMatcher extends QueryParamDecoderMatcher[String]("query")

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  def apply(config: Config, servers: ServerMap)(implicit system: ActorSystem,
                                                logger: Logger,
                                                domainActor: ActorRef,
                                                db: Transactor.Aux[IO, Unit]): IO[ExitCode] = {
    val ec = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(4))

    val routes: HttpRoutes[IO] =
      HttpRoutes.of[IO] {
        case GET -> Root => IndexHandler()
        case GET -> Root / "search" :? QueryParamMatcher(query) =>
          SearchHandler.html(SearchRequest(query), servers)
        case GET -> Root / "api" / "search" :? QueryParamMatcher(query) =>
          SearchHandler.api(SearchRequest(query), servers)
        case GET -> Root / "random" => RandomHandler()
        case req @ GET -> Root / "styles.css" =>
          StaticFile.fromResource("/styles.css", ec, Some(req)).getOrElseF(NotFound())
      }

    BlazeServerBuilder[IO]
      .bindHttp(config.env.port, config.env.host)
      .withHttpApp(routes.orNotFound)
      .serve
      .compile
      .drain
      .map(_ => ExitCode.Success)
  }
}
