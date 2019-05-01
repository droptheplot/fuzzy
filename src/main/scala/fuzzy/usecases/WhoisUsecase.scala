package fuzzy.usecases

import akka.actor.ActorRef
import cats.data.NonEmptyList
import cats.effect.{Async, Effect, IO}
import cats.syntax.applicative._
import doobie.implicits._
import doobie.util.transactor.Transactor
import fuzzy.actors.DomainActor
import fuzzy.entities._
import fuzzy.repositories.DomainRepositoryTrait
import fuzzy.services.WhoisServiceTrait
import fuzzy.utils.Traversable._
import org.slf4j.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class WhoisUsecase[F[_]: Effect: Async]()(implicit whoisService: WhoisServiceTrait,
                                          domainRepository: DomainRepositoryTrait,
                                          serverMap: ServerMap)
    extends WhoisUsecaseTrait[F] {
  def search(searchRequest: SearchRequest)(
      implicit logger: Logger,
      domainActor: ActorRef,
      xa: Transactor.Aux[IO, _],
  ): F[Seq[SearchResponse]] =
    whoisService.parseDomain(searchRequest.query, serverMap) match {
      case Some(Domain(sld, tld)) =>
        logger.info(s"SearchHandler.apply sld=$sld tld=$tld")

        lazy val commonTLDs: NonEmptyList[TLD] = whoisService.commonTLDs(tld)

        (for {
          cache <- domainRepository.get(sld, commonTLDs).transact(xa)
          searchResponses <- fetch(commonTLDs, sld, cache)
          similarSearchResponses <- domainRepository.soundex(sld).transact(xa)
        } yield {
          searchResponses
            .flatMap(_.toOption)
            .doall(domainActor ! DomainActor.CreateMessage(_, xa, logger))
            .++(similarSearchResponses)
        }).to[F]
      case None => Seq[SearchResponse]().pure[F]
    }

  def random()(implicit xa: Transactor.Aux[IO, _]): F[Option[SearchResponse]] =
    domainRepository.random().transact(xa).to[F]

  private def fetch(commonTLDs: NonEmptyList[TLD], sld: String, cache: Set[SearchResponse])(
      implicit logger: Logger): IO[Seq[Try[SearchResponse]]] =
    IO.async[Seq[Try[SearchResponse]]] { cb =>
      Future
        .traverse(commonTLDs.toList) { tld =>
          cache.find(_.tld == tld) match {
            case None                 => Future { whoisService.get(sld, tld, serverMap(tld)) }
            case Some(searchResponse) => Future.successful(Success(searchResponse))
          }
        }
        .onComplete {
          case Success(value) => cb(Right(value))
          case Failure(error) => cb(Left(error))
        }
    }
}
