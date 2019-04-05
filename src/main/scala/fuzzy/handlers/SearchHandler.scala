package fuzzy.handlers

import akka.actor.ActorRef
import cats.effect.IO
import doobie.util.transactor.Transactor
import fuzzy.entities.{SearchRequest, SearchResponse}
import fuzzy.services.WhoisService.ServerMap
import fuzzy.templates.{LayoutTemplate, SearchTemplate}
import fuzzy.usecases.WhoisUsecase
import org.http4s.{MediaType, Response}
import org.http4s.dsl.io._
import org.http4s.headers.`Content-Type`
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
