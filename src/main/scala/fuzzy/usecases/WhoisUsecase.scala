package fuzzy.usecases

import akka.actor.ActorRef
import cats.data.NonEmptyList
import cats.effect.IO
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

class WhoisUsecase()(implicit whoisService: WhoisServiceTrait,
                     domainRepository: DomainRepositoryTrait,
                     serverMap: ServerMap)
    extends WhoisUsecaseTrait {
  def search(searchRequest: SearchRequest)(
      implicit logger: Logger,
      domainActor: ActorRef,
      db: Transactor.Aux[IO, _],
  ): IO[Seq[SearchResponse]] =
    whoisService.parseDomain(searchRequest.query, serverMap) match {
      case Some(Domain(sld, tld)) =>
        logger.info(s"SearchHandler.apply sld=$sld tld=$tld")

        lazy val commonTLDs: NonEmptyList[TLD] = whoisService.commonTLDs(tld)

        for {
          cache <- domainRepository.get(sld, commonTLDs).transact(db)
          searchResponses <- fetch(commonTLDs, sld, cache)
          similarSearchResponses <- domainRepository.soundex(sld).transact(db)
        } yield {
          searchResponses
            .flatMap(_.toOption)
            .doall(domainActor ! DomainActor.CreateMessage(_, db, logger))
            .++(similarSearchResponses)
        }
      case None => IO.pure(Seq[SearchResponse]())
    }

  def random()(implicit db: Transactor.Aux[IO, _]): IO[Option[SearchResponse]] =
    domainRepository.random().transact(db)

  private def fetch(commonTLDs: NonEmptyList[TLD], sld: String, cache: Set[SearchResponse])(
      implicit logger: Logger): IO[Seq[Try[SearchResponse]]] = IO.async { cb =>
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
