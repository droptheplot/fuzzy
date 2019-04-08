package fuzzy

import scala2html.Tag
import scala2html.implicits._

package object templates {
  def icon(name: String, prefix: String = "fas"): Tag =
    <("i", ("class", s"$prefix fa-$name"))
}
