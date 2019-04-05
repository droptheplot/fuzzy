package fuzzy.usecases

import akka.actor.ActorRef
import cats.data.NonEmptyList
import cats.effect.IO
import cats.implicits._
import doobie.implicits._
import doobie.util.transactor.Transactor
import fuzzy.actors.DomainActor
import fuzzy.entities.{SearchRequest, SearchResponse}
import fuzzy.repositories.DomainRepository
import fuzzy.services.WhoisService
import fuzzy.services.WhoisService.{ServerMap, TLD}
import fuzzy.utils.Traversable._
import org.slf4j.Logger

object WhoisUsecase {
  def search(searchRequest: SearchRequest, servers: ServerMap)(
      implicit logger: Logger,
      domainActor: ActorRef,
      db: Transactor.Aux[IO, Unit],
  ): IO[Seq[SearchResponse]] =
    WhoisService.parseDomain(searchRequest.query, servers) match {
      case Some(domain) =>
        logger.info(s"SearchHandler.apply sld=${domain.sld} tld=${domain.tld}")

        lazy val commonTLDs: NonEmptyList[TLD] = WhoisService.commonTLDs(domain.tld)
        lazy val cache: IO[Set[SearchResponse]] = DomainRepository.get(domain.sld, commonTLDs).transact(db)

        commonTLDs.toList
          .map { tld =>
            cache.flatMap(_.find(_.tld == tld) match {
              case None =>
                WhoisService
                  .get(domain.sld, tld, servers(tld))
                  .flatMap(_ match {
                    case Some(raw) => IO.pure(Some(SearchResponse(domain.sld, tld, WhoisService.status(raw), raw)))
                    case None      => IO.pure(None)
                  })
              case searchResponse => IO.pure(searchResponse)
            })
          }
          .sequence
          .flatMap { searchResponses =>
            DomainRepository.soundex(domain.sld).transact(db).flatMap { similarSearchResponses =>
              IO.pure(
                searchResponses.flatten
                  .doall(domainActor ! DomainActor.CreateMessage(_, db, logger))
                  .++(similarSearchResponses)
              )
            }
          }

      case None => IO.pure(Seq[SearchResponse]())
    }

  def random()(implicit db: Transactor.Aux[IO, Unit]): IO[Option[SearchResponse]] =
    DomainRepository.random().transact(db)
}
