package com.handlers

import akka.actor.ActorRef
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, Uri}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.StandardRoute
import cats.effect.IO
import com.actors.DomainActor
import com.entities.{SearchRequest, SearchResponse}
import com.templates.{LayoutTemplate, SearchTemplate}
import com.usecases.WhoisUsecase
import com.utils.Iterable.Doall
import doobie.util.transactor.Transactor
import org.slf4j.Logger

object SearchHandler {
  def apply(searchRequest: SearchRequest, path: Uri.Path, servers: com.usecases.WhoisUsecase.ServerMap)(
      implicit logger: Logger,
      domainActor: ActorRef,
      db: Transactor.Aux[IO, Unit]): StandardRoute = {

    logger.info(path.toString)

    val result: Seq[SearchResponse] = WhoisUsecase
      .parseDomain(searchRequest.query, servers) match {
      case Some(domain) =>
        logger.info(s"domain=$domain")

        WhoisUsecase
          .commonTLDs(domain.tld)
          .par
          .map(tld => (tld, WhoisUsecase.get(domain.sld, tld, servers(tld))))
          .filter { case (_, raw) => raw.isDefined }
          .map { case (tld, raw) => (tld, WhoisUsecase.status(raw.get), raw.get) }
          .map { case (tld, status, raw) => SearchResponse(domain.copy(tld = Some(tld)), status, raw) }
          .toList
          .doall(response => domainActor ! DomainActor.CreateMessage(response, db, logger))
      case None => Seq[SearchResponse]()
    }

    val template = LayoutTemplate(SearchTemplate(searchRequest, result)).toString

    complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, template))
  }
}
