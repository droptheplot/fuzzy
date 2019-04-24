package fuzzy.repositories

import doobie.free.connection.ConnectionIO

trait LDRepositoryTrait {
  def find(value: String): ConnectionIO[Option[Int]]
  def create(value: String): ConnectionIO[Int]
  def findOrCreate(value: String): ConnectionIO[Int]
}
