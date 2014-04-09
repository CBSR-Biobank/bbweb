package domain.validator

import domain.User

import scalaz._
import scalaz.Scalaz._

object UserValidator {

  val emailRegex = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?".r

  def apply(user: User): ValidationNel[String, User] = {
    (validateEmail(user) |@| validateAvatarUrl(user)) { case (v1, v2) => v1 }
  }

  private def validateEmail(user: User): ValidationNel[String, User] = {
    emailRegex.findFirstIn(user.email) match {
      case Some(e) => user.success
      case None => s"Invalid email address: ${user.email} ".failNel
    }
  }

  private def validateAvatarUrl(user: User): ValidationNel[String, User] = {
    if (!user.avatarUrl.isEmpty) {
      s"invalid avatar url: ${user.avatarUrl}".failNel
    } else {
      user.success
    }
  }
}