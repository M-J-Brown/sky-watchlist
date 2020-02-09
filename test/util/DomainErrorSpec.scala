package util

import cats.data.EitherT
import cats.implicits._
import org.scalatestplus.play.PlaySpec
import play.api.test.Helpers.{contentAsJson, contentType, status, _}
import util.DomainError._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DomainErrorSpec extends PlaySpec {
  "asyncResponseToJsonResponse" should {
    "return an error message" in {
      val testError: DomainError = new DomainError {
        override def message: String = "Test Error Message"

        override def status: Int = 400
      }

      val response: AsyncResponse[String] = EitherT.leftT[Future, String](testError)
      val responseF = DomainError.asyncResponseToJsonResponse(response)
      status(responseF) mustBe 400
      contentType(responseF) mustBe Some("application/json")
      contentAsJson(responseF).as[String] mustBe "Test Error Message"
    }

    "return ok for valid response" in {
      val response: AsyncResponse[String] = EitherT.rightT[Future, DomainError]("Success!")
      val responseF = DomainError.asyncResponseToJsonResponse(response)
      status(responseF) mustBe 200
      contentType(responseF) mustBe Some("application/json")
      contentAsJson(responseF).as[String] mustBe "Success!"
    }
  }
}
