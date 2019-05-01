package fuzzy.handlers

import akka.actor.ActorRef
import cats.effect.{Effect, IO}
import cats.syntax.flatMap._
import doobie.util.transactor.Transactor
import fuzzy.entities._
import fuzzy.templates.{LayoutTemplate, SearchTemplate}
import fuzzy.usecases.WhoisUsecaseTrait
import io.circe.{Encoder, Json}
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.{MediaType, Response}
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`
import org.slf4j.Logger

class SearchHandler[F[_]: Effect]()(implicit whoisUsecase: WhoisUsecaseTrait[F]) extends Http4sDsl[F] {
  implicit val encodeStatus: Encoder[Status] = (status: Status) => Json.fromString(status.value)

  def html(searchRequest: SearchRequest)(implicit logger: Logger,
                                         domainActor: ActorRef,
                                         xa: Transactor.Aux[IO, _]): F[Response[F]] = {
    whoisUsecase.search(searchRequest).flatMap { result =>
      val template = LayoutTemplate(s"Search: ${searchRequest.query}", SearchTemplate(searchRequest, result)).toString

      Ok(template, `Content-Type`(MediaType.text.html))
    }
  }

  def api(searchRequest: SearchRequest)(implicit logger: Logger,
                                        domainActor: ActorRef,
                                        xa: Transactor.Aux[IO, _]): F[Response[F]] = {
    whoisUsecase.search(searchRequest).flatMap { result =>
      Ok(result.asJson.noSpaces, `Content-Type`(MediaType.application.json))
    }
  }
}
