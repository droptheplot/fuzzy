package fuzzy.utils

import scala.collection.TraversableLike

object Traversable {
  implicit class Doall[A, Repr](val coll: TraversableLike[A, Repr]) {

    /** Executes function f for every element, but returns original collection. */
    def doall(f: A => Unit): Repr = {
      coll.foreach(f); coll.repr
    }
  }
}
