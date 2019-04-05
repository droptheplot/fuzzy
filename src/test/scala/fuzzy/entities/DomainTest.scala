package fuzzy.entities

import org.scalatest.{FunSpec, Matchers}

class DomainTest extends FunSpec with Matchers {
  describe("toPath") {
    it("should return merged name and tld") {
      val testCases = List[(Domain, String)](
        (Domain("google", Some("com")), "google.com"),
        (Domain("google", None), "google"),
      )

      for ((domain, result) <- testCases) {
        domain.toString should be(result)
      }
    }
  }
}
