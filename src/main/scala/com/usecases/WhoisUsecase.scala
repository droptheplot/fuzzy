package com.usecases

import akka.actor.ActorRef
import cats.data.NonEmptyList
import cats.effect.IO
import com.actors.DomainActor
import com.entities.{SearchRequest, SearchResponse}
import com.repositories.DomainRepository
import com.services.WhoisService
import com.services.WhoisService.{ServerMap, TLD}
import com.utils.Iterable.Doall
import doobie.implicits._
import doobie.util.transactor.Transactor
import org.slf4j.Logger

object WhoisUsecase {
  def search(searchRequest: SearchRequest, servers: ServerMap)(
      implicit logger: Logger,
      domainActor: ActorRef,
      db: Transactor.Aux[IO, Unit],
  ): Seq[SearchResponse] =
    WhoisService.parseDomain(searchRequest.query, servers) match {
      case Some(domain) =>
        logger.info(s"SearchHandler.apply sld=${domain.sld} tld=${domain.tld}")

        val commonTLDs: NonEmptyList[TLD] = WhoisService.commonTLDs(domain.tld)
        val cache: Set[SearchResponse] = DomainRepository.get(domain.sld, commonTLDs).transact(db).unsafeRunSync

        commonTLDs.toList.par
          .flatMap { tld =>
            cache.find(_.tld == tld) match {
              case None =>
                WhoisService.get(domain.sld, tld, servers(tld)) match {
                  case Some(raw) => Some(SearchResponse(domain.sld, tld, WhoisService.status(raw), raw))
                  case None      => None
                }
              case searchResponse => searchResponse
            }
          }
          .toList
          .doall(domainActor ! DomainActor.CreateMessage(_, db, logger))
          .++(DomainRepository.soundex(domain.sld).transact(db).unsafeRunSync)
          .toSeq
      case None => Seq[SearchResponse]()
    }

  def random()(implicit logger: Logger, db: Transactor.Aux[IO, Unit]): Option[SearchResponse] =
    DomainRepository.random().transact(db).unsafeRunSync
}
