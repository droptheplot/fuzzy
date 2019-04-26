package fuzzy.usecases

import akka.actor.ActorRef
import cats.effect.IO
import doobie.util.transactor.Transactor
import fuzzy.entities.{SearchRequest, SearchResponse}
import org.slf4j.Logger

trait WhoisUsecaseTrait {
  def search(searchRequest: SearchRequest)(
      implicit logger: Logger,
      domainActor: ActorRef,
      db: Transactor.Aux[IO, _],
  ): IO[Seq[SearchResponse]]
  def random()(implicit db: Transactor.Aux[IO, _]): IO[Option[SearchResponse]]
}
