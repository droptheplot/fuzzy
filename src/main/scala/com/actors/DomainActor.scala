package com.actors

import akka.actor.Actor
import cats.effect.IO
import com.entities.SearchResponse
import com.repositories.{DomainRepository, SLDRepository, TLDRepository}
import doobie.implicits._
import doobie.util.transactor.Transactor
import org.slf4j.Logger

class DomainActor extends Actor {
  def receive: PartialFunction[Any, Unit] = {
    case DomainActor.CreateMessage(response, db, logger) if response.domain.tld.isDefined =>
      val sldId: Int = SLDRepository.findOrCreate(response.domain.sld).transact(db).unsafeRunSync
      val tldId: Int = TLDRepository.findOrCreate(response.domain.tld.get).transact(db).unsafeRunSync

      DomainRepository
        .create(sldId, tldId, response.status.value, response.raw)
        .transact(db)
        .attempt
        .unsafeRunSync match {
        case Right(id) => logger.info(s"id=$id domain=${response.domain.toString}")
        case Left(e)   => logger.error(e.toString)
      }

    case _ => throw new IllegalArgumentException("Invalid message.")
  }
}

object DomainActor {
  final case class CreateMessage(response: SearchResponse, db: Transactor.Aux[IO, Unit], logger: Logger)
}
