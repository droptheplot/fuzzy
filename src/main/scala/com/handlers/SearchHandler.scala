package com.handlers

import akka.actor.ActorRef
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, Uri}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.StandardRoute
import cats.effect.IO
import com.entities.{SearchRequest, SearchResponse}
import com.services.WhoisService.ServerMap
import com.templates.{LayoutTemplate, SearchTemplate}
import com.usecases.WhoisUsecase
import doobie.util.transactor.Transactor
import org.slf4j.Logger

object SearchHandler {
  def apply(searchRequest: SearchRequest, path: Uri.Path, servers: ServerMap)(
      implicit logger: Logger,
      domainActor: ActorRef,
      db: Transactor.Aux[IO, Unit]): StandardRoute = {

    val result: Seq[SearchResponse] = WhoisUsecase.search(searchRequest, servers)

    val template = LayoutTemplate(s"Search: ${searchRequest.query}", SearchTemplate(searchRequest, result)).toString

    complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, template))
  }
}
