package fuzzy.repositories

import cats.data.NonEmptyList
import doobie.free.connection.ConnectionIO
import fuzzy.entities.SearchResponse

trait DomainRepositoryTrait {
  def create(sldId: Int, tldId: Int, status: String, raw: String): ConnectionIO[Int]
  def get(sld: String, tlds: NonEmptyList[String]): ConnectionIO[Set[SearchResponse]]
  def soundex(value: String): ConnectionIO[List[SearchResponse]]
  def random(): ConnectionIO[Option[SearchResponse]]
}
