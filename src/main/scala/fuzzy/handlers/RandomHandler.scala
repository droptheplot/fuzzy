package fuzzy.handlers

import cats.effect.{Effect, IO}
import cats.syntax.flatMap._
import doobie.util.transactor.Transactor
import fuzzy.usecases.WhoisUsecaseTrait
import org.http4s.{Query, Response, Uri}
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Location
import org.slf4j.Logger

class RandomHandler[F[_]: Effect]()(implicit whoisUsecase: WhoisUsecaseTrait[F]) extends Http4sDsl[F] {
  def apply()(implicit logger: Logger, xa: Transactor.Aux[IO, _]): F[Response[F]] =
    whoisUsecase.random().flatMap {
      case Some(searchResponse) =>
        SeeOther(Location(Uri(path = "/search", query = Query.fromPairs(("query", searchResponse.sld)))))
      case None =>
        SeeOther(Location(Uri.uri("/")))
    }
}
