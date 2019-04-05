package fuzzy.entities

sealed case class Status(value: String)

object Status {
  object Available extends Status("available")
  object Taken extends Status("taken")
}
