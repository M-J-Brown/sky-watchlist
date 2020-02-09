package service

import javax.inject.{Inject, Singleton}
import mouse.boolean.booleanSyntaxMouse
import play.api.http.Status.BAD_REQUEST
import play.api.libs.json.{Format, Json, Reads, Writes}
import service.WatchlistService.{ContentId, CustomerId}
import util.DomainError
import util.DomainError.SyncResponse

@Singleton class WatchlistService @Inject()() {

  def validateCustomerId(maybeValidId: String): SyncResponse[CustomerId] = CustomerId.fromString(maybeValidId)

  def validateContentId(maybeValidId: String): SyncResponse[ContentId] = ContentId.fromString(maybeValidId)

}

object WatchlistService {

  private def isAlphaNumericOfLength(string: String, length: Int): Boolean = string.length == length && string.forall(_.isLetterOrDigit)

  case class CustomerId(underlying: String) extends AnyVal

  case class ContentId(underlying: String) extends AnyVal

  object CustomerId {
    private val reads: Reads[CustomerId] = json => implicitly[Reads[String]].reads(json).map(CustomerId(_))
    private val writes: Writes[CustomerId] = id => Json.toJson(id.underlying)
    implicit val format: Format[CustomerId] = Format(reads, writes)

    def fromString(string: String): SyncResponse[CustomerId] =
      isAlphaNumericOfLength(string, 3).either(InvalidCustomerIdError(string), CustomerId(string))

    case class InvalidCustomerIdError(id: String) extends DomainError {
      override val status: Int = BAD_REQUEST
      override val message: String = s"$id is not a valid customerId"
    }

  }

  object ContentId {
    private val reads: Reads[ContentId] = json => implicitly[Reads[String]].reads(json).map(ContentId(_))
    private val writes: Writes[ContentId] = id => Json.toJson(id.underlying)
    implicit val format: Format[ContentId] = Format(reads, writes)

    def fromString(string: String): SyncResponse[ContentId] =
      isAlphaNumericOfLength(string, 5).either(InvalidContentIdError(string), ContentId(string))

    case class InvalidContentIdError(id: String) extends DomainError {
      override val status: Int = BAD_REQUEST
      override val message: String = s"$id is not a valid contentId"
    }

  }

}