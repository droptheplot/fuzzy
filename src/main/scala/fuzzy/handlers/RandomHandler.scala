package fuzzy.handlers

import cats.effect.IO
import doobie.util.transactor.Transactor
import fuzzy.usecases.WhoisUsecaseTrait
import org.http4s.{Query, Response, Uri}
import org.http4s.dsl.io._
import org.http4s.headers.Location
import org.slf4j.Logger

class RandomHandler()(implicit whoisUsecase: WhoisUsecaseTrait) {
  def apply()(implicit logger: Logger, db: Transactor.Aux[IO, Unit]): IO[Response[IO]] =
    whoisUsecase.random().flatMap {
      case Some(searchResponse) =>
        SeeOther(Location(Uri(path = "/search", query = Query.fromPairs(("query", searchResponse.sld)))))
      case None =>
        SeeOther(Location(Uri.uri("/")))
    }
}
