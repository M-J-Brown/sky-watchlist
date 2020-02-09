import controllers.WatchlistController
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test.Helpers._
import play.api.test._
import respository.WatchlistRepository
import service.WatchlistService

import scala.concurrent.ExecutionContext.Implicits.global

class AcceptanceCriteria extends PlaySpec with GuiceOneAppPerTest with Injecting {

  private def getController: WatchlistController = new WatchlistController(service, repo, stubControllerComponents())

  private def service: WatchlistService = inject[WatchlistService]

  private def repo: WatchlistRepository = inject[WatchlistRepository]

  "/watchlists/:customerId GET" should {
    "return an empty watchlist for a valid id" in {
      val controllerToTest = getController
      val responseF = controllerToTest.watchlist("123").apply(FakeRequest(GET, "/"))

      status(responseF) mustBe OK
      contentType(responseF) mustBe Some("application/json")
      contentAsJson(responseF).as[Set[String]] mustBe Set.empty
    }

    "remember a watchlist of valid ids" in {
      val controllerToTest = getController

      val responseF = for {
        _ <- controllerToTest.addToWatchlist("123", "zRE49").apply(FakeRequest(POST, "/"))
        _ <- controllerToTest.addToWatchlist("123", "wYqiZ").apply(FakeRequest(POST, "/"))
        _ <- controllerToTest.addToWatchlist("123", "15nW5").apply(FakeRequest(POST, "/"))
        _ <- controllerToTest.addToWatchlist("123", "srT5k").apply(FakeRequest(POST, "/"))
        _ <- controllerToTest.addToWatchlist("123", "FBSxr").apply(FakeRequest(POST, "/"))
        response <- controllerToTest.watchlist("123").apply(FakeRequest(GET, "/"))
      } yield response


      status(responseF) mustBe OK
      contentType(responseF) mustBe Some("application/json")
      contentAsJson(responseF).as[Set[String]] mustBe Set("zRE49", "wYqiZ", "15nW5", "srT5k", "FBSxr")
    }

    "allow removing an entry from a watchlist" in {
      val controllerToTest = getController

      val responseF = for {
        _ <- controllerToTest.addToWatchlist("123", "zRE49").apply(FakeRequest(POST, "/"))
        _ <- controllerToTest.addToWatchlist("123", "wYqiZ").apply(FakeRequest(POST, "/"))
        _ <- controllerToTest.addToWatchlist("123", "15nW5").apply(FakeRequest(POST, "/"))
        _ <- controllerToTest.addToWatchlist("123", "srT5k").apply(FakeRequest(POST, "/"))
        _ <- controllerToTest.addToWatchlist("123", "FBSxr").apply(FakeRequest(POST, "/"))
        _ <- controllerToTest.removeFromWatchlist("123", "15nW5").apply(FakeRequest(DELETE, "/"))
        response <- controllerToTest.watchlist("123").apply(FakeRequest(GET, "/"))
      } yield response

      status(responseF) mustBe OK
      contentType(responseF) mustBe Some("application/json")
      contentAsJson(responseF).as[Set[String]] mustBe Set("zRE49", "wYqiZ", "srT5k", "FBSxr")
    }

    "track watchlists separately for each customer" in {
      val controllerToTest = getController

      val responseF = for {
        _ <- controllerToTest.addToWatchlist("123", "zRE49").apply(FakeRequest(POST, "/"))
        _ <- controllerToTest.addToWatchlist("123", "wYqiZ").apply(FakeRequest(POST, "/"))
        _ <- controllerToTest.addToWatchlist("123", "srT5k").apply(FakeRequest(POST, "/"))
        _ <- controllerToTest.addToWatchlist("123", "FBSxr").apply(FakeRequest(POST, "/"))
        _ <- controllerToTest.addToWatchlist("abc", "hWjNK").apply(FakeRequest(POST, "/"))
        _ <- controllerToTest.addToWatchlist("abc", "U8jVg").apply(FakeRequest(POST, "/"))
        _ <- controllerToTest.addToWatchlist("abc", "GH4pD").apply(FakeRequest(POST, "/"))
        _ <- controllerToTest.addToWatchlist("abc", "rGIha").apply(FakeRequest(POST, "/"))
        response <- controllerToTest.watchlist("abc").apply(FakeRequest(GET, "/"))
      } yield response

      status(responseF) mustBe OK
      contentType(responseF) mustBe Some("application/json")
      contentAsJson(responseF).as[Set[String]] mustBe Set("hWjNK", "U8jVg", "GH4pD", "rGIha")
    }
  }
}