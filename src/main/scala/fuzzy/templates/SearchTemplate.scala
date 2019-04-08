package fuzzy.templates

import fuzzy.entities.{SearchRequest, SearchResponse}
import fuzzy.entities.Status._
import scala2html.Tag
import scala2html.implicits._

import scala.language.postfixOps

object SearchTemplate {
  def apply(searchRequest: SearchRequest, result: Seq[SearchResponse]): Seq[Tag] = Seq[Tag](
    <("nav", ("class", "navbar navbar-light bg-white border-bottom mb-3")) > (
      <("div", ("class", "container")) > (
        <("a", ("class", "navbar-brand"), ("href", "/")) > "FUZZY",
        <("form", ("class", "form-inline m-0"), ("id", "search"), ("method", "get"), ("action", "/search")) > (
          <("input",
            ("type", "text"),
            ("name", "query"),
            ("class", "form-control mr-1"),
            ("placeholder", "Search"),
            ("value", searchRequest.query)) />,
          <("input", ("type", "submit"), ("class", "btn btn-primary"), ("value", "Search domains")) />,
        ),
      ),
    ),
    <("div", ("class", "container text-center")) > (
      <("div", ("class", "row")) > (result.map { searchResponse =>
        searchResponse.status match {
          case Available => cardAvailable(searchResponse)
          case _         => cardTaken(searchResponse)
        }
      }: _*),
    ),
    <("div", ("class", "text-center py-5")) > (
      <("a", ("class", "navbar-text text-muted"), ("href", "https://github.com/droptheplot/fuzzy")) >
        icon("github", "fab"),
    )
  )

  def cardAvailable(searchResponse: SearchResponse): Tag = {
    <("div", ("class", "col-md-3 mb-3")) > (
      <("div", ("class", "p-3 text-success border rounded bg-white")) > (
        <("span", ("class", "mr-3")) > icon("check"),
        searchResponse.toString,
      )
    )
  }

  def cardTaken(searchResponse: SearchResponse): Tag = {
    <("div", ("class", "col-md-3 mb-3")) > (
      <("div", ("class", "p-3 text-muted border rounded bg-white")) > (
        <("span", ("class", "mr-3")) > icon("minus"),
        searchResponse.toString,
      )
    )
  }
}
