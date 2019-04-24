package fuzzy.services

trait ClientTrait {
  def connect(hostname: String): Unit
  def disconnect(): Unit
  def query(handle: String): String
}
