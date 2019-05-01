package fuzzy.usecases

import akka.actor.ActorRef
import cats.effect.IO
import doobie.util.transactor.Transactor
import fuzzy.entities.{SearchRequest, SearchResponse}
import org.slf4j.Logger

trait WhoisUsecaseTrait[F[_]] {
  def search(searchRequest: SearchRequest)(
      implicit logger: Logger,
      domainActor: ActorRef,
      xa: Transactor.Aux[IO, _],
  ): F[Seq[SearchResponse]]
  def random()(implicit xa: Transactor.Aux[IO, _]): F[Option[SearchResponse]]
}
