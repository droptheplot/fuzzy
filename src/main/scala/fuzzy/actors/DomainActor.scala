package fuzzy.actors

import akka.actor.Actor
import cats.effect.IO
import doobie.implicits._
import doobie.util.transactor.Transactor
import fuzzy.entities.SearchResponse
import fuzzy.repositories.{DomainRepository, SLDRepository, TLDRepository}
import org.slf4j.Logger

class DomainActor extends Actor {
  def receive: PartialFunction[Any, Unit] = {
    case DomainActor.CreateMessage(response, db, logger) =>
      val sldId: Int = SLDRepository.findOrCreate(response.sld).transact(db).unsafeRunSync
      val tldId: Int = TLDRepository.findOrCreate(response.tld).transact(db).unsafeRunSync

      new DomainRepository()
        .create(sldId, tldId, response.status.value, response.raw)
        .transact(db)
        .attempt
        .unsafeRunSync match {
        case Right(id) => logger.info(s"DomainActor.receive id=$id sld=${response.sld}, tld=${response.tld}")
        case Left(e)   => logger.error(s"DomainActor.receive error=${e.toString}")
      }

    case _ => throw new IllegalArgumentException("Invalid message.")
  }
}

object DomainActor {
  final case class CreateMessage(response: SearchResponse, db: Transactor.Aux[IO, Unit], logger: Logger)
}
