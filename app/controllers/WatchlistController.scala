package controllers

import cats.implicits._
import javax.inject._
import play.api.mvc._
import respository.WatchlistRepository
import service.WatchlistService

import scala.concurrent.ExecutionContext

@Singleton
class WatchlistController @Inject()(service: WatchlistService,
                                    repository: WatchlistRepository,
                                    override val controllerComponents: ControllerComponents)
                                   (implicit ec: ExecutionContext) extends BaseController {

  def watchlist(customerId: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    for {
      validId <- service.validateCustomerId(customerId).toAsync
      watchlist <- repository.forCustomerId(validId)
    } yield watchlist
  }

  def addToWatchlist(customerId: String, contentId: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    for {
      validCustomerId <- service.validateCustomerId(customerId).toAsync
      validContentId <- service.validateContentId(contentId).toAsync
      _ <- repository.add(validCustomerId, validContentId)
    } yield "Added"
  }

  def removeFromWatchlist(customerId: String, contentId: String): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      for {
        validCustomerId <- service.validateCustomerId(customerId).toAsync
        validContentId <- service.validateContentId(contentId).toAsync
        _ <- repository.remove(validCustomerId, validContentId)
      } yield "Removed"
  }
}
