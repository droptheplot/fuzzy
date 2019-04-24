package fuzzy.repositories

import cats.data.NonEmptyList
import doobie.Fragments
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import fuzzy.entities.SearchResponse

class DomainRepository extends DomainRepositoryTrait {

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

  def random(): ConnectionIO[Option[SearchResponse]] =
    sql"""
      SELECT s.value, t.value, domains.status, domains.raw
      FROM domains
      TABLESAMPLE SYSTEM(10)
      LEFT JOIN slds s on domains.sld_id = s.id
      LEFT JOIN tlds t on domains.tld_id = t.id
      WHERE status = 'available'
      LIMIT 1;
    """.query[SearchResponse].option
}
