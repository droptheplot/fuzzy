package com.handlers

import akka.http.scaladsl.model.{StatusCodes, Uri}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.StandardRoute
import cats.effect.IO
import com.usecases.WhoisUsecase
import doobie.util.transactor.Transactor
import org.slf4j.Logger

object RandomHandler {
  def apply(path: Uri.Path)(implicit logger: Logger, db: Transactor.Aux[IO, Unit]): StandardRoute =
    WhoisUsecase.random() match {
      case Some(searchResponse) => redirect(s"/search?query=${searchResponse.sld}", StatusCodes.SeeOther)
      case None                 => redirect("/", StatusCodes.SeeOther)
    }
}
