package fuzzy.repositories

import cats.data.OptionT
import doobie.free.connection.ConnectionIO
import doobie.implicits._

object TLDRepository extends LDRepository {
  def find(value: String): ConnectionIO[Option[Int]] =
    sql"""SELECT id FROM tlds WHERE value = $value LIMIT 1;""".query[Int].option

  def create(value: String): ConnectionIO[Int] =
    sql"""INSERT INTO tlds (value) VALUES ($value) RETURNING id;""".update.withUniqueGeneratedKeys[Int]("id")

  def findOrCreate(value: String): ConnectionIO[Int] =
    OptionT[ConnectionIO, Int](find(value)).getOrElseF(create(value))
}
