package fuzzy.actors

import akka.actor.Actor
import cats.effect.IO
import doobie.implicits._
import doobie.util.transactor.Transactor
import fuzzy.entities.SearchResponse
import fuzzy.repositories.{DomainRepository, DomainRepositoryTrait, SLDRepository, TLDRepository}
import org.slf4j.Logger

class DomainActor extends Actor {
  def receive: PartialFunction[Any, Unit] = {
    case DomainActor.CreateMessage(response, db, logger) =>
      val domainRepository: DomainRepositoryTrait = new DomainRepository()

      (for {
        sldId <- SLDRepository.findOrCreate(response.sld)
        tldId <- TLDRepository.findOrCreate(response.tld)
        domainId <- domainRepository.create(sldId, tldId, response.status.value, response.raw)
      } yield {
        logger.info(s"DomainActor.receive id=$domainId sld=${response.sld}, tld=${response.tld}")
      }).transact(db).unsafeRunSync()

    case _ => throw new IllegalArgumentException("Invalid message.")
  }
}

object DomainActor {
  final case class CreateMessage(response: SearchResponse, db: Transactor.Aux[IO, _], logger: Logger)
}
