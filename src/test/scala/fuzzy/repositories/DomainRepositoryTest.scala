package fuzzy.repositories

import cats.data.NonEmptyList
import doobie.scalatest._
import org.scalatest.{FunSuite, Matchers}
import pureconfig.generic.auto._

class DomainRepositoryTest extends FunSuite with Matchers with IOChecker with RepositoryTest {
  val domainRepository: DomainRepositoryTrait = new DomainRepository()

  test("create") { domainRepository.create(1, 1, "Available", "Raw") }
  test("get") { domainRepository.get("google", NonEmptyList.one("com")) }
  test("soundex") { domainRepository.soundex("google") }
  test("random") { domainRepository.random() }
}
