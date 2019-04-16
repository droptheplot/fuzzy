package fuzzy.repositories

import cats.data.NonEmptyList
import doobie.scalatest._
import org.scalatest.{FunSuite, Matchers}
import pureconfig.generic.auto._

class DomainRepositoryTest extends FunSuite with Matchers with IOChecker with RepositoryTest {
  test("create") { DomainRepository.create(1, 1, "Available", "Raw") }
  test("get") { DomainRepository.get("google", NonEmptyList.one("com")) }
  test("soundex") { DomainRepository.soundex("google") }
  test("random") { DomainRepository.random() }
}
