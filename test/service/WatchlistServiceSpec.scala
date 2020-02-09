package service

import org.scalatest.{Matchers, WordSpec}
import service.WatchlistService.{ContentId, CustomerId}

class WatchlistServiceSpec extends WordSpec with Matchers {

  def service = new WatchlistService()

  "CustomerId" should {
    "accept valid ids" in {
      service.validateCustomerId("123") shouldBe Right(CustomerId("123"))
    }
    "reject ids with symbols" in {
      service.validateCustomerId("!23").isLeft shouldBe true
    }
    "reject short ids" in {
      service.validateCustomerId("12").isLeft shouldBe true
    }
    "reject long ids" in {
      service.validateCustomerId("12345").isLeft shouldBe true
    }
  }

  "ContentId" should {
    "accept valid ids" in {
      service.validateContentId("12345") shouldBe Right(ContentId("12345"))
    }
    "reject ids with symbols" in {
      service.validateContentId("!2345").isLeft shouldBe true
    }
    "reject short ids" in {
      service.validateContentId("1234").isLeft shouldBe true
    }
    "reject long ids" in {
      service.validateContentId("1234567").isLeft shouldBe true
    }
  }

}
