package com.repositories

import doobie.free.connection.ConnectionIO
import doobie.implicits._

object DomainRepository {
  def create(sldId: Int, tldId: Int, status: String, raw: String): ConnectionIO[Int] = {
    sql"""
      INSERT INTO domains (sld_id, tld_id, status, checked_at, raw)
      VALUES ($sldId, $tldId, $status, NOW(), $raw)
      ON CONFLICT (tld_id, sld_id) DO UPDATE
      SET checked_at = NOW();
    """.update.run
  }
}
