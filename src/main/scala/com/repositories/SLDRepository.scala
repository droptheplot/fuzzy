package com.repositories

import cats.data.OptionT
import doobie.free.connection.ConnectionIO
import doobie.implicits._

object SLDRepository extends LDRepository {
  def find(value: String): ConnectionIO[Option[Int]] =
    sql"""SELECT id FROM slds WHERE value = $value LIMIT 1;""".query[Int].option

  def create(value: String): ConnectionIO[Int] =
    sql"""INSERT INTO slds (value) VALUES ($value) RETURNING id;""".update.withUniqueGeneratedKeys[Int]("id")

  def findOrCreate(value: String): ConnectionIO[Int] =
    OptionT[ConnectionIO, Int](find(value)).getOrElseF(create(value))
}
