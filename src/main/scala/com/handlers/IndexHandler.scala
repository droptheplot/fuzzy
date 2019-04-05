package com.handlers

import cats.effect.IO
import com.templates.{IndexTemplate, LayoutTemplate}
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.headers._
import org.slf4j.Logger

object IndexHandler {
  def apply()(implicit logger: Logger): IO[Response[IO]] = {
    val template = LayoutTemplate("Fuzzy domain search engine", IndexTemplate()).toString

    Ok(template, `Content-Type`(MediaType.text.html))
  }
}
