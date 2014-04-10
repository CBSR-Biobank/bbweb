package domain.validator

import domain._
import domain.User

import scalaz._
import scalaz.Scalaz._

trait UserValidator extends Validator {

  val emailRegex = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?".r
  val urlRegex = "^((https?|ftp)://|(www|ftp)\\.)[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$".r

  def validateId(id: UserId): Validation[String, UserId] = {
    val idString = id.toString
    if ((idString == null) || idString.isEmpty()) {
      s"id is null or empty".failNel
    }
    id.success
  }

  def validateEmail(email: String): Validation[String, String] = {
    emailRegex.findFirstIn(email) match {
      case Some(e) => email.success
      case None => s"Invalid email address: $email".fail
    }
  }

  def validateAvatarUrl(url: Option[String]): Validation[String, Option[String]] = {
    url match {
      case Some(url) =>
        urlRegex.findFirstIn(url) match {
          case Some(e) => some(url).success
          case None => s"invalid avatar url: $url".fail
        }
      case None =>
        none.success
    }
  }
}