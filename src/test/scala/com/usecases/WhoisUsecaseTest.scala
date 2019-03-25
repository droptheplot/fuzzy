package com.usecases

import com.entities.Domain
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers}
import org.slf4j.Logger

class WhoisUsecaseTest extends FunSpec with MockFactory with Matchers {
  describe("get") {
    implicit val client: WhoisUsecase.ClientTrait = mock[WhoisUsecase.ClientTrait]
    implicit val logger: Logger = mock[Logger]
    val server: WhoisUsecase.Server = WhoisUsecase.Server("whois.verisign-grs.com")

    describe("when query is successful") {
      it("should return whois data") {
        (client.connect _).expects("whois.verisign-grs.com").atLeastOnce()
        (client.query _).expects("google.com").returning("Whois about google.com").atLeastOnce()
        (client.disconnect _).expects().atLeastOnce()

        WhoisUsecase.get("google", "com", server) should be(Some("Whois about google.com"))
      }
    }

    describe("when query throws") {
      it("should return None") {
        (client.connect _).expects("whois.verisign-grs.com").atLeastOnce()
        (client.query _).expects("google.com").throwing(new Exception).atLeastOnce()

        WhoisUsecase.get("google", "com", server) should be(None)
      }
    }
  }

  describe("status") {
    describe("when taken") {
      it("should return Taken") {
        val raw =
          """Domain Name: google.com
            |Registry Domain ID
          """.stripMargin

        WhoisUsecase.status(raw) should be(WhoisUsecase.Status.Taken)
      }
    }

    describe("when available") {
      it("should return Available") {
        val testCases = List[String](
          """NOT FOUND
            |Last update of WHOIS database
          """.stripMargin,
          """No match for
            |>>> Last update of whois database
          """.stripMargin,
        )

        for (raw <- testCases) {
          WhoisUsecase.status(raw) should be(WhoisUsecase.Status.Available)
        }
      }
    }
  }

  describe("parseDomain") {
    val servers: WhoisUsecase.ServerMap = Map[WhoisUsecase.TLD, WhoisUsecase.Server](
      "com" -> WhoisUsecase.Server("whois.verisign-grs.com")
    )

    it("should create Domain with name and tld") {
      val testCases = List[(String, Option[Domain])](
        ("www.google.com", Some(Domain("google", Some("com")))),
        ("google.com", Some(Domain("google", Some("com")))),
        ("google.", Some(Domain("google", None))),
        ("google", Some(Domain("google", None))),
        ("", None),
      )

      for ((str, result) <- testCases) {
        WhoisUsecase.parseDomain(str, servers) should be(result)
      }
    }
  }

  describe("commonServerList") {
    describe("when TLD is common") {
      it("should move it to first position") {
        WhoisUsecase.commonTLDs(Some("org")) should be(List[WhoisUsecase.TLD]("org", "com", "net", "co", "io", "app"))
      }
    }

    describe("when TLD is not common") {
      it("should put it on first position") {
        WhoisUsecase.commonTLDs(Some("ru")) should be(
          List[WhoisUsecase.TLD]("ru", "com", "net", "org", "co", "io", "app"))
      }
    }

    describe("when TLD is not given") {
      it("should not modify order") {
        WhoisUsecase.commonTLDs(None) should be(List[WhoisUsecase.TLD]("com", "net", "org", "co", "io", "app"))
      }
    }
  }
}
