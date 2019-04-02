package com.handlers

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, Uri}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.StandardRoute
import com.templates.{IndexTemplate, LayoutTemplate}
import org.slf4j.Logger

object IndexHandler {
  def apply(path: Uri.Path)(implicit logger: Logger): StandardRoute = {
    val template = LayoutTemplate("Fuzzy domain search engine", IndexTemplate()).toString

    logger.info("IndexHanndler.apply")

    complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, template))
  }
}
