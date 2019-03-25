package com.handlers

import akka.actor.ActorRef
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, Uri}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.StandardRoute
import cats.data.NonEmptyList
import cats.effect.IO
import com.actors.DomainActor
import com.entities.{SearchRequest, SearchResponse}
import com.repositories.DomainRepository
import com.templates.{LayoutTemplate, SearchTemplate}
import com.usecases.WhoisUsecase
import com.usecases.WhoisUsecase.TLD
import com.utils.Iterable.Doall
import doobie.util.transactor.Transactor
import org.slf4j.Logger
import doobie.implicits._

object SearchHandler {
  def apply(searchRequest: SearchRequest, path: Uri.Path, servers: com.usecases.WhoisUsecase.ServerMap)(
      implicit logger: Logger,
      domainActor: ActorRef,
      db: Transactor.Aux[IO, Unit]): StandardRoute = {

    val result: Seq[SearchResponse] = WhoisUsecase
      .parseDomain(searchRequest.query, servers) match {
      case Some(domain) =>
        logger.info(s"SearchHandler.apply sld=${domain.sld} tld=${domain.tld}")

        val commonTLDs: NonEmptyList[TLD] = WhoisUsecase.commonTLDs(domain.tld)
        val cache: Set[SearchResponse] = DomainRepository.get(domain.sld, commonTLDs).transact(db).unsafeRunSync

        commonTLDs.toList.par
          .flatMap { tld =>
            cache.find(_.tld == tld) match {
              case None =>
                WhoisUsecase.get(domain.sld, tld, servers(tld)) match {
                  case Some(raw) => Some(SearchResponse(domain.sld, tld, WhoisUsecase.status(raw), raw))
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

    val template = LayoutTemplate(SearchTemplate(searchRequest, result)).toString

    complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, template))
  }
}
