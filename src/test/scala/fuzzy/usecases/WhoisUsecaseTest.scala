package fuzzy.usecases

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{TestActors, TestKitBase}
import cats.data.NonEmptyList
import cats.effect.{Async, ContextShift, IO}
import doobie.free.connection.ConnectionIO
import doobie.util.transactor.{Strategy, Transactor}
import fuzzy.entities._
import fuzzy.repositories.{DomainRepository, DomainRepositoryTrait}
import fuzzy.services.{ClientTrait, WhoisService, WhoisServiceTrait}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSpec, Matchers}
import org.slf4j.Logger

import scala.concurrent.ExecutionContext
import scala.util.Success

class WhoisUsecaseTest
    extends FunSpec
    with MockFactory
    with Matchers
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with TestKitBase {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  implicit lazy val system: ActorSystem = ActorSystem()

  implicit val echoRef: ActorRef = system.actorOf(TestActors.echoActorProps)

  implicit val db: Transactor.Aux[IO, _] =
    Transactor
      .fromConnection[IO](null, null)
      .copy(strategy0 = Strategy.void)

  implicit val serverMap: ServerMap = Map[TLD, Server](
    "com" -> Server("whois.verisign-grs.com"),
    "org" -> Server("whois.pir.org")
  )

  val commonTLDs: NonEmptyList[TLD] = NonEmptyList.fromListUnsafe(serverMap.keys.toList)

  val googleCom: SearchResponse = SearchResponse("google", "com", null, null)
  val googleOrg: SearchResponse = SearchResponse("google", "org", null, null)
  val gogoleCom: SearchResponse = SearchResponse("gogole", "com", null, null)

  implicit var whoisService: WhoisServiceTrait = _
  implicit var domainRepository: DomainRepositoryTrait = _
  implicit var logger: Logger = _

  var whoisUsecase: WhoisUsecaseTrait = _

  override def beforeEach(): Unit = {
    whoisService = mock[WhoisService]
    domainRepository = mock[DomainRepository]
    logger = mock[Logger]

    whoisUsecase = new WhoisUsecase()
  }

  describe("search") {
    describe("with only sld") {
      it("should return valid responses") {
        val query: String = "google.com"

        inSequence {
          (whoisService.parseDomain _)
            .expects(query, serverMap)
            .returning(Some(Domain("google", None)))

          (logger.info: String => Unit)
            .expects("SearchHandler.apply sld=google tld=None")

          (whoisService.commonTLDs _)
            .expects(None)
            .returning(commonTLDs)

          (domainRepository.get _)
            .expects("google", commonTLDs)
            .returning(Async[ConnectionIO].liftIO(IO.pure(Set[SearchResponse](googleCom))))

          (whoisService
            .get(_: SLD, _: TLD, _: Server)(_: ClientTrait, _: Logger))
            .expects("google", "org", serverMap("org"), *, *)
            .returning(Success(googleOrg))

          (domainRepository.soundex _)
            .expects("google")
            .returning(Async[ConnectionIO].liftIO(IO.pure(List[SearchResponse](gogoleCom))))
        }

        whoisUsecase.search(SearchRequest(query)).unsafeRunSync should be(
          Seq[SearchResponse](googleCom, googleOrg, gogoleCom))
      }
    }

    describe("with sld and tld") {
      it("should return valid responses") {
        val query: String = "google.org"

        inSequence {
          (whoisService.parseDomain _)
            .expects(query, serverMap)
            .returning(Some(Domain("google", Some("org"))))

          (logger.info: String => Unit)
            .expects("SearchHandler.apply sld=google tld=Some(org)")

          (whoisService.commonTLDs _)
            .expects(Some("org"))
            .returning(commonTLDs)

          (domainRepository.get _)
            .expects("google", commonTLDs)
            .returning(Async[ConnectionIO].liftIO(IO.pure(Set[SearchResponse](googleCom))))

          (whoisService
            .get(_: SLD, _: TLD, _: Server)(_: ClientTrait, _: Logger))
            .expects("google", "org", serverMap("org"), *, *)
            .returning(Success(googleOrg))

          (domainRepository.soundex _)
            .expects("google")
            .returning(Async[ConnectionIO].liftIO(IO.pure(List[SearchResponse](gogoleCom))))
        }

        whoisUsecase.search(SearchRequest(query)).unsafeRunSync should be(
          Seq[SearchResponse](googleCom, googleOrg, gogoleCom))
      }
    }
  }

  override def afterAll: Unit = {
    shutdown(system)
  }
}
