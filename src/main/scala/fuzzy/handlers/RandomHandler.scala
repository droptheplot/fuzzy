package fuzzy.handlers

import cats.effect.IO
import doobie.util.transactor.Transactor
import fuzzy.usecases.WhoisUsecase
import org.http4s.{Query, Response, Uri}
import org.http4s.dsl.io._
import org.http4s.headers.Location
import org.slf4j.Logger

object RandomHandler {
  def apply()(implicit logger: Logger, db: Transactor.Aux[IO, Unit]): IO[Response[IO]] =
    WhoisUsecase.random() match {
      case Some(searchResponse) =>
        SeeOther(Location(Uri(path = "/search", query = Query.fromPairs(("query", searchResponse.sld)))))
      case None =>
        SeeOther(Location(Uri.uri("/")))
    }
}
