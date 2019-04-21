package fuzzy.usecases

import akka.actor.ActorRef
import cats.data.NonEmptyList
import cats.effect.IO
import doobie.implicits._
import doobie.util.transactor.Transactor
import fuzzy.actors.DomainActor
import fuzzy.entities.{Domain, SearchRequest, SearchResponse}
import fuzzy.repositories.DomainRepository
import fuzzy.services.WhoisService
import fuzzy.services.WhoisService.{ServerMap, TLD}
import fuzzy.utils.Traversable._
import org.slf4j.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object WhoisUsecase {
  def search(searchRequest: SearchRequest, servers: ServerMap)(
      implicit logger: Logger,
      domainActor: ActorRef,
      db: Transactor.Aux[IO, Unit],
  ): IO[Seq[SearchResponse]] =
    WhoisService.parseDomain(searchRequest.query, servers) match {
      case Some(Domain(sld, tld)) =>
        logger.info(s"SearchHandler.apply sld=$sld tld=$tld")

        lazy val commonTLDs: NonEmptyList[TLD] = WhoisService.commonTLDs(tld)

        for {
          cache <- DomainRepository.get(sld, commonTLDs).transact(db)
          searchResponses <- fetch(commonTLDs, sld, cache, servers)
          similarSearchResponses <- DomainRepository.soundex(sld).transact(db)
        } yield {
          searchResponses
            .flatMap(_.toOption)
            .doall(domainActor ! DomainActor.CreateMessage(_, db, logger))
            .++(similarSearchResponses)
        }
      case None => IO.pure(Seq[SearchResponse]())
    }

  def random()(implicit db: Transactor.Aux[IO, Unit]): IO[Option[SearchResponse]] =
    DomainRepository.random().transact(db)

  private def fetch(commonTLDs: NonEmptyList[TLD], sld: String, cache: Set[SearchResponse], servers: ServerMap)(
      implicit logger: Logger): IO[Seq[Try[SearchResponse]]] = IO.async { cb =>
    Future
      .traverse(commonTLDs.toList) { tld =>
        cache.find(_.tld == tld) match {
          case None                 => Future { WhoisService.get(sld, tld, servers(tld)) }
          case Some(searchResponse) => Future.successful(Success(searchResponse))
        }
      }
      .onComplete {
        case Success(value) => cb(Right(value))
        case Failure(error) => cb(Left(error))
      }
  }
}
