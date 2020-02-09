package controllers

import cats.data.EitherT
import cats.implicits._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{MustMatchers, WordSpec}
import play.api.test.Helpers._
import play.api.test._
import respository.WatchlistRepository
import service.WatchlistService
import service.WatchlistService.CustomerId.InvalidCustomerIdError
import service.WatchlistService.{ContentId, CustomerId}
import util.DomainError

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class WatchlistControllerSpec extends WordSpec with MockFactory with MustMatchers {

  "watchlist" should {
    "return the watchlist for a valid customer id" in {
      val service = mock[WatchlistService]
      (service.validateCustomerId _).expects(*).onCall { s: String => Right(CustomerId(s)) }
      val repo = mock[WatchlistRepository]
      (repo.forCustomerId _).expects(*).returning(EitherT.rightT[Future, DomainError](Set(ContentId("ContentId"))))
      val responseF = new WatchlistController(service, repo, stubControllerComponents()).watchlist("an id").apply(FakeRequest(GET, "/"))
      status(responseF) mustBe OK
      contentType(responseF) mustBe Some("application/json")
      contentAsJson(responseF).as[Set[String]] mustBe Set("ContentId")
    }

    "bad request for an invalid customer id" in {
      val service = mock[WatchlistService]
      (service.validateCustomerId _).expects(*).onCall { s: String => Left(InvalidCustomerIdError(s)) }
      val repo = mock[WatchlistRepository]
      val responseF = new WatchlistController(service, repo, stubControllerComponents()).watchlist("an id").apply(FakeRequest(GET, "/"))
      status(responseF) mustBe BAD_REQUEST
      contentType(responseF) mustBe Some("application/json")
      contentAsJson(responseF).as[String] mustBe "an id is not a valid customerId"
    }
  }

  "addToWatchlist" should {
    "actually add to the repository" in {
      val service = mock[WatchlistService]
      (service.validateCustomerId _).expects(*).onCall { s: String => Right(CustomerId(s)) }
      (service.validateContentId _).expects(*).onCall { s: String => Right(ContentId(s)) }
      val repo = mock[WatchlistRepository]
      (repo.add _).expects(*, *).returning(EitherT.rightT[Future, DomainError]())
      val responseF = new WatchlistController(service, repo, stubControllerComponents()).addToWatchlist("an id", "an id").apply(FakeRequest(GET, "/"))
      status(responseF) mustBe OK
      contentType(responseF) mustBe Some("application/json")
      contentAsJson(responseF).as[String] mustBe "Added"
    }
  }

  "removeFromWatchlist" should {
    "actually remove from the repository" in {
      val service = mock[WatchlistService]
      (service.validateCustomerId _).expects(*).onCall { s: String => Right(CustomerId(s)) }
      (service.validateContentId _).expects(*).onCall { s: String => Right(ContentId(s)) }
      val repo = mock[WatchlistRepository]
      (repo.remove _).expects(*, *).returning(EitherT.rightT[Future, DomainError]())
      val responseF = new WatchlistController(service, repo, stubControllerComponents()).removeFromWatchlist("an id", "an id").apply(FakeRequest(GET, "/"))
      status(responseF) mustBe OK
      contentType(responseF) mustBe Some("application/json")
      contentAsJson(responseF).as[String] mustBe "Removed"
    }
  }
}
