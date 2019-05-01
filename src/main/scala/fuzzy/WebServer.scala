package fuzzy

import java.util.concurrent.Executors

import akka.actor.{ActorRef, ActorSystem}
import cats.effect._
import cats.Applicative
import doobie.util.transactor.Transactor
import fs2.Stream
import fuzzy.entities._
import fuzzy.handlers._
import fuzzy.repositories.{DomainRepository, DomainRepositoryTrait}
import fuzzy.services.{WhoisService, WhoisServiceTrait}
import fuzzy.usecases.{WhoisUsecase, WhoisUsecaseTrait}
import org.http4s.{HttpRoutes, StaticFile}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeServerBuilder
import org.slf4j.Logger
import pureconfig.generic.auto._

import scala.concurrent.ExecutionContext

class WebServer[F[_]: Applicative: ContextShift: ConcurrentEffect] extends Http4sDsl[F] {
  def run(config: Config)(implicit serverMap: ServerMap,
                          system: ActorSystem,
                          logger: Logger,
                          domainActor: ActorRef,
                          xa: Transactor.Aux[IO, _]): Stream[F, ExitCode] = {
    object QueryParamMatcher extends QueryParamDecoderMatcher[String]("query")

    val ec = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(4))

    implicit val whoisService: WhoisServiceTrait = new WhoisService()

    implicit val domainRepository: DomainRepositoryTrait = new DomainRepository()

    implicit val whoisUsecase: WhoisUsecaseTrait[F] = new WhoisUsecase()

    implicit val searchHandler: SearchHandler[F] = new SearchHandler()
    implicit val randomHandler: RandomHandler[F] = new RandomHandler()
    implicit val indexHandler: IndexHandler[F] = new IndexHandler()

    val routes: HttpRoutes[F] =
      HttpRoutes.of[F] {
        case GET -> Root => indexHandler()
        case GET -> Root / "search" :? QueryParamMatcher(query) =>
          searchHandler.html(SearchRequest(query))
        case GET -> Root / "api" / "search" :? QueryParamMatcher(query) =>
          searchHandler.api(SearchRequest(query))
        case GET -> Root / "random" => randomHandler()
        case req @ GET -> Root / "styles.css" =>
          StaticFile.fromResource("/styles.css", ec, Some(req)).getOrElseF(NotFound())
      }

    BlazeServerBuilder[F]
      .bindHttp(config.env.port, config.env.host)
      .withHttpApp(routes.orNotFound)
      .serve
  }
}
