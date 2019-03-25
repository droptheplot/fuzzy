package com.repositories

import cats.data.NonEmptyList
import com.entities.SearchResponse
import doobie.free.connection.ConnectionIO
import doobie._
import doobie.implicits._

object DomainRepository {

  /** Returns id */
  def create(sldId: Int, tldId: Int, status: String, raw: String): ConnectionIO[Int] =
    sql"""
      INSERT INTO domains (sld_id, tld_id, status, checked_at, raw)
      VALUES ($sldId, $tldId, $status, NOW(), $raw)
      ON CONFLICT (tld_id, sld_id) DO UPDATE
      SET checked_at = NOW();
      """.update.run

  def get(sld: String, tlds: NonEmptyList[String]): ConnectionIO[Set[SearchResponse]] =
    (fr"""
      SELECT s.value, t.value, domains.status, domains.raw
      FROM domains
      LEFT JOIN slds s ON domains.sld_id = s.id
      LEFT JOIN tlds t ON domains.tld_id = t.id
      WHERE s.value = $sld
        AND """ ++ Fragments.in(fr"t.value", tlds)).query[SearchResponse].to[Set]

  def soundex(value: String): ConnectionIO[List[SearchResponse]] =
    sql"""
      SELECT s.value, t.value, domains.status, domains.raw
      FROM domains
      LEFT JOIN slds s ON domains.sld_id = s.id
      LEFT JOIN tlds t ON domains.tld_id = t.id
      WHERE soundex(s.value) = soundex($value)
        AND s.value != $value
        AND status = 'available';
      """.query[SearchResponse].to[List]
}
