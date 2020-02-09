package util

import cats.data.EitherT
import cats.implicits._
import play.api.libs.json.{Json, Writes}
import play.api.mvc.Result
import play.api.mvc.Results._

import scala.concurrent.{ExecutionContext, Future}

trait DomainError {
  def message: String

  def status: Int
}

object DomainError {
  type AsyncResponse[T] = EitherT[Future, DomainError, T]
  type SyncResponse[T] = Either[DomainError, T]

  implicit class SyncResponseOps[T](response: SyncResponse[T]) {
    def toAsync(implicit ec: ExecutionContext): EitherT[Future, DomainError, T] = EitherT.fromEither[Future](response)
  }

  implicit def asyncResponseToJsonResponse[T: Writes](response: AsyncResponse[T])(implicit ec: ExecutionContext): Future[Result] = response.value.map {
    case Left(error) => new Status(error.status)(Json.toJson(error.message))
    case Right(value) => Ok(Json.toJson(value))
  }

}
