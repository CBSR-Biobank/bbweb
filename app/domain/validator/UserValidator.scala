package domain.validator

import domain._
import domain.User

import scalaz._
import scalaz.Scalaz._

object UserValidator extends Validator {

  val emailRegex = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?".r
  val urlRegex = "^((https?|ftp)://|(www|ftp)\\.)[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$".r

  def apply(user: User): DomainValidation[User] = {
    (validateNonEmpty("name", user.name) |@|
      validateEmail(user) |@|
      validateNonEmpty("password", user.password) |@|
      validateAvatarUrl(user)) { case (_, _, _, v) => v }
  }

  private def validateEmail(user: User): DomainValidation[User] = {
    emailRegex.findFirstIn(user.email) match {
      case Some(e) => user.success
      case None => s"Invalid email address: ${user.email}".failNel
    }
  }

  private def validateAvatarUrl(user: User): DomainValidation[User] = {
    user.avatarUrl match {
      case Some(url) =>
        urlRegex.findFirstIn(url) match {
          case Some(e) => user.success
          case None => s"invalid avatar url: ${user.avatarUrl}".failNel
        }
      case None =>
        user.success
    }
  }
}