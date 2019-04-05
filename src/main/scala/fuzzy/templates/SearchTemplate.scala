package fuzzy.templates

import fuzzy.entities.{SearchRequest, SearchResponse}
import fuzzy.entities.Status._
import scala2html.Tag
import scala2html.implicits._

import scala.language.postfixOps

object SearchTemplate {
  def apply(searchRequest: SearchRequest, result: Seq[SearchResponse]): Seq[Tag] = Seq[Tag](
    <("form", ("id", "search"), ("method", "get"), ("action", "/search")) > (
      <("div", ("class", "form-row")) > (
        <("div", ("class", "form-group m-0 col-md-4")) > (
          <("input",
            ("type", "text"),
            ("name", "query"),
            ("class", "form-control"),
            ("placeholder", "Search"),
            ("value", searchRequest.query)) />,
        ),
        <("div", ("class", "form-group m-0 col-md-2")) > (
          <("input", ("type", "submit"), ("class", "btn btn-primary btn-block"), ("value", "Search domains")) />,
        ),
        <("div", ("class", "form-group m-0 col-md-6")),
      ),
    ),
    <("div", ("class", "row")) > (result.map { searchResponse =>
      searchResponse.status match {
        case Available => cardAvailable(searchResponse)
        case _         => cardTaken(searchResponse)
      }
    }: _*),
  )

  def cardAvailable(searchResponse: SearchResponse): Tag = {
    <("div", ("class", "col-md-3 mb-3")) > (
      <("div", ("class", "p-3 text-success border rounded")) > (
        <("span", ("class", "mr-3")) > icon("check"),
        searchResponse.toString,
      )
    )
  }

  def cardTaken(searchResponse: SearchResponse): Tag = {
    <("div", ("class", "col-md-3 mb-3")) > (
      <("div", ("class", "p-3 text-muted border rounded")) > (
        <("span", ("class", "mr-3")) > icon("minus"),
        searchResponse.toString,
      )
    )
  }
}
