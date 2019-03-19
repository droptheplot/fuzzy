package com.utils

object Iterable {
  implicit class Doall[T](val coll: Seq[T]) {

    /** Executes function op for every element, but returns original collection. */
    def doall(op: T => Unit): Seq[T] = {
      for (el <- coll) op(el); coll
    }
  }
}
