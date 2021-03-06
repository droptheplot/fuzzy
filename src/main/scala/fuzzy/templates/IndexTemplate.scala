package fuzzy.templates

import scala2html.Tag
import scala2html.implicits._

import scala.language.postfixOps

object IndexTemplate {
  def apply(): Seq[Tag] = Seq[Tag](
    <("div", ("class", "container text-center py-5")) > (
      <("div", ("class", "py-5")) > (
        <("a", ("href", "/"), ("class", "logo text-dark")) > (
          "FUZZY",
          <("div", ("class", "text-muted")) > "DOMAIN SEARCH ENGINE",
        ),
      ),
      <("div", ("class", "row")) > (
        <("div", ("class", "col")),
        <("div", ("class", "col-6")) > (
          <("form", ("id", "search"), ("method", "get"), ("action", "/search")) > (
            <("div", ("class", "form-group")) > (
              <("input", ("type", "text"), ("name", "query"), ("class", "form-control"), ("placeholder", "Search")) />,
            ),
            <("div", ("class", "form-group text-center")) > (
              <("input", ("type", "submit"), ("class", "btn btn-primary mx-1"), ("value", "Search domains")) />,
              <("a", ("href", "/random"), ("class", "btn btn-link mx-1")) > "I'm feeling lucky",
            ),
          ),
        ),
        <("div", ("class", "col")),
      ),
    )
  )
}
