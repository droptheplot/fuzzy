package fuzzy.utils

object Iterable {
  implicit class Doall[T](val coll: Iterable[T]) {

    /** Executes function op for every element, but returns original collection. */
    def doall(op: T => Unit): Iterable[T] = {
      for (el <- coll) op(el); coll
    }
  }
}
