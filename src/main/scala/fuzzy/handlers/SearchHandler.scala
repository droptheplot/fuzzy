package fuzzy.handlers

import akka.actor.ActorRef
import cats.effect.IO
import doobie.util.transactor.Transactor
import fuzzy.entities._
import fuzzy.templates.{LayoutTemplate, SearchTemplate}
import fuzzy.usecases.WhoisUsecaseTrait
import io.circe.{Encoder, Json}
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.{MediaType, Response}
import org.http4s.dsl.io._
import org.http4s.headers.`Content-Type`
import org.slf4j.Logger

class SearchHandler()(implicit whoisUsecase: WhoisUsecaseTrait) {
  implicit val encodeStatus: Encoder[Status] = (status: Status) => Json.fromString(status.value)

  def html(searchRequest: SearchRequest)(implicit logger: Logger,
                                         domainActor: ActorRef,
                                         db: Transactor.Aux[IO, _]): IO[Response[IO]] = {
    whoisUsecase.search(searchRequest).flatMap { result =>
      val template = LayoutTemplate(s"Search: ${searchRequest.query}", SearchTemplate(searchRequest, result)).toString

      Ok(template, `Content-Type`(MediaType.text.html))
    }
  }

  def api(searchRequest: SearchRequest)(implicit logger: Logger,
                                        domainActor: ActorRef,
                                        db: Transactor.Aux[IO, _]): IO[Response[IO]] = {
    whoisUsecase.search(searchRequest).flatMap { result =>
      Ok(result.asJson.noSpaces, `Content-Type`(MediaType.application.json))
    }
  }
}
