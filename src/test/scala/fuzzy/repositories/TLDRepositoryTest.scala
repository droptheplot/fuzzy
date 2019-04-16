package fuzzy.repositories

import doobie.scalatest._
import org.scalatest.{FunSuite, Matchers}
import pureconfig.generic.auto._

class TLDRepositoryTest extends FunSuite with Matchers with IOChecker with RepositoryTest {
  test("find") { TLDRepository.find("google") }
  test("create") { TLDRepository.create("google") }
  test("findOrCreate") { TLDRepository.findOrCreate("google") }
}
