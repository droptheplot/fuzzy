package fuzzy.handlers

import akka.actor.ActorRef
import cats.effect.IO
import doobie.util.transactor.Transactor
import fuzzy.entities.{SearchRequest, Status}
import fuzzy.services.WhoisService.ServerMap
import fuzzy.templates.{LayoutTemplate, SearchTemplate}
import fuzzy.usecases.WhoisUsecase
import io.circe.{Encoder, Json}
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.{MediaType, Response}
import org.http4s.dsl.io._
import org.http4s.headers.`Content-Type`
import org.slf4j.Logger

object SearchHandler {
  implicit val encodeStatus: Encoder[Status] = (status: Status) => Json.fromString(status.value)

  def html(searchRequest: SearchRequest, servers: ServerMap)(implicit logger: Logger,
                                                             domainActor: ActorRef,
                                                             db: Transactor.Aux[IO, Unit]): IO[Response[IO]] = {
    WhoisUsecase.search(searchRequest, servers).flatMap { result =>
      val template = LayoutTemplate(s"Search: ${searchRequest.query}", SearchTemplate(searchRequest, result)).toString

      Ok(template, `Content-Type`(MediaType.text.html))
    }
  }

  def api(searchRequest: SearchRequest, servers: ServerMap)(implicit logger: Logger,
                                                            domainActor: ActorRef,
                                                            db: Transactor.Aux[IO, Unit]): IO[Response[IO]] = {
    WhoisUsecase.search(searchRequest, servers).flatMap { result =>
      Ok(result.asJson.noSpaces, `Content-Type`(MediaType.application.json))
    }
  }
}
