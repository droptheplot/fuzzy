package fuzzy

import java.util.concurrent.Executors

import akka.actor.{ActorRef, ActorSystem}
import cats.effect.{ContextShift, ExitCode, IO}
import doobie.util.transactor.Transactor
import fuzzy.entities._
import fuzzy.handlers._
import fuzzy.repositories.{DomainRepository, DomainRepositoryTrait}
import fuzzy.services.{WhoisService, WhoisServiceTrait}
import fuzzy.usecases.{WhoisUsecase, WhoisUsecaseTrait}
import org.http4s.{HttpRoutes, StaticFile}
import org.http4s.dsl.io._
import org.http4s.server.blaze.BlazeServerBuilder
import org.slf4j.Logger
import pureconfig.generic.auto._

import scala.concurrent.ExecutionContext

object WebServer {
  def apply(config: Config)(implicit serverMap: ServerMap,
                            system: ActorSystem,
                            logger: Logger,
                            domainActor: ActorRef,
                            db: Transactor.Aux[IO, Unit]): IO[ExitCode] = {
    object QueryParamMatcher extends QueryParamDecoderMatcher[String]("query")

    implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

    val ec = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(4))

    implicit val whoisService: WhoisServiceTrait = new WhoisService()

    implicit val domainRepository: DomainRepositoryTrait = new DomainRepository()

    implicit val whoisUsecase: WhoisUsecaseTrait = new WhoisUsecase()

    implicit val searchHandler: SearchHandler = new SearchHandler()
    implicit val randomHandler: RandomHandler = new RandomHandler()
    implicit val indexHandler: IndexHandler = new IndexHandler()

    val routes: HttpRoutes[IO] =
      HttpRoutes.of[IO] {
        case GET -> Root => indexHandler()
        case GET -> Root / "search" :? QueryParamMatcher(query) =>
          searchHandler.html(SearchRequest(query))
        case GET -> Root / "api" / "search" :? QueryParamMatcher(query) =>
          searchHandler.api(SearchRequest(query))
        case GET -> Root / "random" => randomHandler()
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
