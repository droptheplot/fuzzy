package fuzzy.handlers

import cats.effect.Effect
import fuzzy.templates.{IndexTemplate, LayoutTemplate}
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers._
import org.slf4j.Logger

class IndexHandler[F[_]: Effect] extends Http4sDsl[F] {
  def apply()(implicit logger: Logger): F[Response[F]] = {
    val template = LayoutTemplate("Fuzzy domain search engine", IndexTemplate()).toString

    Ok(template, `Content-Type`(MediaType.text.html))
  }
}
