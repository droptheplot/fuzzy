package fuzzy.services

import cats.data.NonEmptyList
import fuzzy.entities.{Domain, Status}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers}
import org.slf4j.Logger

class WhoisServiceTest extends FunSpec with MockFactory with Matchers {
  describe("get") {
    implicit val client: WhoisService.ClientTrait = mock[WhoisService.ClientTrait]
    implicit val logger: Logger = mock[Logger]
    val server: WhoisService.Server = WhoisService.Server("whois.verisign-grs.com")

    describe("when query is successful") {
      it("should return whois data") {
        (client.connect _).expects("whois.verisign-grs.com").once()
        (client.query _).expects("google.com").returning("Whois about google.com").once()
        (client.disconnect _).expects().once()
        (logger.info: String => Unit).expects("WhoisUsecase.get sld=google tld=com").once()

        for (result <- WhoisService.get("google", "com", server)) yield {
          result should be(Some("Whois about google.com"))
        }
      }
    }

    describe("when query throws") {
      it("should return None") {
        (client.connect _).expects("whois.verisign-grs.com").once()
        (client.query _).expects("google.com").throwing(new Exception).once()
        (logger.info: String => Unit).expects("WhoisUsecase.get sld=google tld=com").once()

        for (result <- WhoisService.get("google", "com", server)) yield {
          result should be(None)
        }
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

        WhoisService.status(raw) should be(Status.Taken)
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
          WhoisService.status(raw) should be(Status.Available)
        }
      }
    }
  }

  describe("parseDomain") {
    val servers: WhoisService.ServerMap = Map[WhoisService.TLD, WhoisService.Server](
      "com" -> WhoisService.Server("whois.verisign-grs.com")
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
        WhoisService.parseDomain(str, servers) should be(result)
      }
    }
  }

  describe("commonServerList") {
    describe("when TLD is common") {
      it("should move it to first position") {
        WhoisService.commonTLDs(Some("org")) should be(
          NonEmptyList.of[WhoisService.TLD]("org", "com", "net", "co", "io", "app"))
      }
    }

    describe("when TLD is not common") {
      it("should put it on first position") {
        WhoisService.commonTLDs(Some("ru")) should be(
          NonEmptyList.of[WhoisService.TLD]("ru", "com", "net", "org", "co", "io", "app"))
      }
    }

    describe("when TLD is not given") {
      it("should not modify order") {
        WhoisService.commonTLDs(None) should be(
          NonEmptyList.of[WhoisService.TLD]("com", "net", "org", "co", "io", "app"))
      }
    }
  }
}
