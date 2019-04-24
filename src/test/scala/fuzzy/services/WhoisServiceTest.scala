package fuzzy.services

import cats.data.NonEmptyList
import fuzzy.entities._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers}
import org.slf4j.Logger

import scala.util.Success

class WhoisServiceTest extends FunSpec with MockFactory with Matchers {
  val whoisService: WhoisServiceTrait = new WhoisService()

  describe("get") {
    implicit val client: ClientTrait = mock[ClientTrait]
    implicit val logger: Logger = mock[Logger]
    val server: Server = Server("whois.verisign-grs.com")

    describe("when query is successful") {
      it("should return whois data") {
        (client.connect _).expects("whois.verisign-grs.com").once()
        (client.query _).expects("google.com").returning("Whois about google.com").once()
        (client.disconnect _).expects().once()
        (logger.info: String => Unit).expects("WhoisUsecase.get sld=google tld=com").once()

        whoisService.get("google", "com", server) should be(
          Success(SearchResponse("google", "com", Status.Available, "Whois about google.com")))
      }
    }

    describe("when query throws") {
      it("should return Failure") {
        (client.connect _).expects("whois.verisign-grs.com").once()
        (client.query _).expects("google.com").throwing(new Exception).once()
        (logger.info: String => Unit).expects("WhoisUsecase.get sld=google tld=com").once()

        whoisService.get("google", "com", server).isFailure should be(true)
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

        whoisService.status(raw) should be(Status.Taken)
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
          whoisService.status(raw) should be(Status.Available)
        }
      }
    }
  }

  describe("parseDomain") {
    val serverMap: ServerMap = Map[TLD, Server]("com" -> Server("whois.verisign-grs.com"))

    it("should create Domain with name and tld") {
      val testCases = List[(String, Option[Domain])](
        ("www.google.com", Some(Domain("google", Some("com")))),
        ("google.com", Some(Domain("google", Some("com")))),
        ("google.", Some(Domain("google", None))),
        ("google", Some(Domain("google", None))),
        ("", None),
      )

      for ((str, result) <- testCases) {
        whoisService.parseDomain(str, serverMap) should be(result)
      }
    }
  }

  describe("commonServerList") {
    describe("when TLD is common") {
      it("should move it to first position") {
        whoisService.commonTLDs(Some("org")) should be(NonEmptyList.of[TLD]("org", "com", "net", "co", "io", "app"))
      }
    }

    describe("when TLD is not common") {
      it("should put it on first position") {
        whoisService.commonTLDs(Some("ru")) should be(
          NonEmptyList.of[TLD]("ru", "com", "net", "org", "co", "io", "app"))
      }
    }

    describe("when TLD is not given") {
      it("should not modify order") {
        whoisService.commonTLDs(None) should be(NonEmptyList.of[TLD]("com", "net", "org", "co", "io", "app"))
      }
    }
  }
}
