package com.templates

import scala2html._

import scala.language.postfixOps

object IndexTemplate {
  def apply(): Seq[Tag] = Seq[Tag](
    <("div", ("class", "row")) > (
      <("div", ("class", "col")),
      <("div", ("class", "col-6")) > (
        <("form", ("id", "search"), ("method", "get"), ("action", "/search")) > (
          <("div", ("class", "form-group")) > (
            <("input", ("type", "text"), ("name", "query"), ("class", "form-control"), ("placeholder", "Search")) />,
          ),
          <("div", ("class", "form-group text-center")) > (
            <("input", ("type", "submit"), ("class", "btn btn-primary mx-1"), ("value", "Search domains")) />,
            <("input", ("type", "submit"), ("class", "btn btn-outline-secondary mx-1"), ("value", "I'm feeling lucky")) />,
          ),
        ),
      ),
      <("div", ("class", "col")),
    )
  )
}
