package fuzzy.usecases

import akka.actor.ActorRef
import cats.data.NonEmptyList
import cats.effect.IO
import cats.implicits._
import doobie.implicits._
import doobie.util.transactor.Transactor
import fuzzy.actors.DomainActor
import fuzzy.entities.{Domain, SearchRequest, SearchResponse}
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
      case Some(Domain(sld, tld)) =>
        logger.info(s"SearchHandler.apply sld=$sld tld=$tld")

        lazy val commonTLDs: NonEmptyList[TLD] = WhoisService.commonTLDs(tld)

        for {
          cache <- DomainRepository.get(sld, commonTLDs).transact(db)
          searchResponses <- commonTLDs.toList.map(fetch(sld, _, cache, servers)).sequence
          similarSearchResponses <- DomainRepository.soundex(sld).transact(db)
        } yield {
          searchResponses.flatten
            .doall(domainActor ! DomainActor.CreateMessage(_, db, logger))
            .++(similarSearchResponses)
        }

      case None => IO.pure(Seq[SearchResponse]())
    }

  def random()(implicit db: Transactor.Aux[IO, Unit]): IO[Option[SearchResponse]] =
    DomainRepository.random().transact(db)

  private def fetch(sld: String, tld: String, cache: Set[SearchResponse], servers: ServerMap)(
      implicit logger: Logger
  ): IO[Option[SearchResponse]] =
    cache.find(_.tld == tld) match {
      case None           => WhoisService.get(sld, tld, servers(tld))
      case searchResponse => IO.pure(searchResponse)
    }
}
