package com.handlers

import akka.actor.ActorRef
import cats.effect.IO
import com.entities.{SearchRequest, SearchResponse}
import com.services.WhoisService.ServerMap
import com.templates.{LayoutTemplate, SearchTemplate}
import com.usecases.WhoisUsecase
import doobie.util.transactor.Transactor
import org.http4s.{MediaType, Response}
import org.http4s.dsl.io._
import org.http4s.headers._
import org.slf4j.Logger

object SearchHandler {
  def apply(searchRequest: SearchRequest, servers: ServerMap)(implicit logger: Logger,
                                                              domainActor: ActorRef,
                                                              db: Transactor.Aux[IO, Unit]): IO[Response[IO]] = {

    val result: Seq[SearchResponse] = WhoisUsecase.search(searchRequest, servers)

    val template = LayoutTemplate(s"Search: ${searchRequest.query}", SearchTemplate(searchRequest, result)).toString

    Ok(template, `Content-Type`(MediaType.text.html))
  }
}
