package repository

import cats.implicits._
import org.scalatest.{Matchers, WordSpec}
import play.api.test.Helpers._
import respository.WatchlistRepository
import respository.WatchlistRepository.{AlreadyInWatchlistError, NotInWatchlistError}
import service.WatchlistService.{ContentId, CustomerId}

import scala.concurrent.ExecutionContext.Implicits.global

class WatchlistRepositorySpec extends WordSpec with Matchers {

  private val testCustomerId1 = CustomerId("123")
  private val testCustomerId2 = CustomerId("abc")
  private val testContentId1 = ContentId("1")
  private val testContentId2 = ContentId("2")

  def newRepo = new WatchlistRepository()

  "WatchlistRepository" should {

    "return empty for a new customer" in {
      await(newRepo.forCustomerId(testCustomerId1).value) shouldBe Right(Set.empty)
    }

    "allow you to add content" in {
      val repo = newRepo
      await((for {
        _ <- repo.add(testCustomerId1, testContentId1)
        _ <- repo.add(testCustomerId1, testContentId2)
        watchlist <- repo.forCustomerId(testCustomerId1)
      } yield watchlist).value) shouldBe Right(Set(testContentId1, testContentId2))
    }

    "not allow you to add the same content id twice" in {
      val repo = newRepo
      await((for {
        _ <- repo.add(testCustomerId1, testContentId1)
        _ <- repo.add(testCustomerId1, testContentId1)
        watchlist <- repo.forCustomerId(testCustomerId1)
      } yield watchlist).value) shouldBe Left(AlreadyInWatchlistError(testContentId1))
    }

    "allow you to remove content" in {
      val repo = newRepo
      await((for {
        _ <- repo.add(testCustomerId1, testContentId1)
        _ <- repo.add(testCustomerId1, testContentId2)
        _ <- repo.remove(testCustomerId1, testContentId2)
        watchlist <- repo.forCustomerId(testCustomerId1)
      } yield watchlist).value) shouldBe Right(Set(testContentId1))
    }

    "not allow you to remove content not in the list" in {
      val repo = newRepo
      await((for {
        _ <- repo.add(testCustomerId1, testContentId1)
        _ <- repo.remove(testCustomerId1, testContentId2)
        watchlist <- repo.forCustomerId(testCustomerId1)
      } yield watchlist).value) shouldBe Left(NotInWatchlistError(testContentId2))
    }

    "track multiple users" in {
      val repo = newRepo
      await((for {
        _ <- repo.add(testCustomerId1, testContentId1)
        _ <- repo.add(testCustomerId2, testContentId2)
        watchlist <- repo.forCustomerId(testCustomerId1)
      } yield watchlist).value) shouldBe Right(Set(testContentId1))
    }
  }
}
