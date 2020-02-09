package respository

import cats.data.EitherT
import cats.implicits._
import javax.inject.{Inject, Singleton}
import mouse.boolean.booleanSyntaxMouse
import play.api.http.Status.BAD_REQUEST
import respository.WatchlistRepository.{AlreadyInWatchlistError, NotInWatchlistError}
import service.WatchlistService.{ContentId, CustomerId}
import util.DomainError
import util.DomainError.AsyncResponse

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

@Singleton class WatchlistRepository @Inject()()(implicit ec: ExecutionContext) {

  //TODO: Concurrent?
  private val storage: mutable.Map[CustomerId, Set[ContentId]] = mutable.Map.empty

  def forCustomerId(customerId: CustomerId): AsyncResponse[Set[ContentId]] = EitherT.rightT[Future, DomainError](
    storage.getOrElse(customerId, Set.empty)
  )

  def add(customerId: CustomerId, contentId: ContentId): AsyncResponse[Unit] = EitherT.fromEither[Future] {
    val current = storage.getOrElse(customerId, Set.empty)
    (!current.contains(contentId)).either(AlreadyInWatchlistError(contentId), storage.put(customerId, current + contentId))
  }

  def remove(customerId: CustomerId, contentId: ContentId): AsyncResponse[Unit] = EitherT.fromEither[Future] {
    val current = storage.getOrElse(customerId, Set.empty)
    current.contains(contentId).either(NotInWatchlistError(contentId), storage.put(customerId, current - contentId))
  }
}

object WatchlistRepository {

  case class AlreadyInWatchlistError(contentId: ContentId) extends DomainError {
    override val message: String = s"${contentId.underlying} is already in the watchlist"

    override def status: Int = BAD_REQUEST
  }

  case class NotInWatchlistError(contentId: ContentId) extends DomainError {
    override val message: String = s"${contentId.underlying} is not in the watchlist"

    override def status: Int = BAD_REQUEST
  }

}
