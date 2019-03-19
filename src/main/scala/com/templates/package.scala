package com

import scala2html._

package object templates {
  def icon(name: String): Tag = <("i", ("class", s"fas fa-$name"))
}
