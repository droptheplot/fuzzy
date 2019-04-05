package fuzzy

import scala2html.Tag
import scala2html.implicits._

package object templates {
  def icon(name: String): Tag = <("i", ("class", s"fas fa-$name"))
}
