package fuzzy.repositories

import doobie.scalatest._
import org.scalatest.{FunSuite, Matchers}
import pureconfig.generic.auto._

class SLDRepositoryTest extends FunSuite with Matchers with IOChecker with RepositoryTest {
  test("find") { SLDRepository.find("google") }
  test("create") { SLDRepository.create("google") }
  test("findOrCreate") { SLDRepository.findOrCreate("google") }
}
