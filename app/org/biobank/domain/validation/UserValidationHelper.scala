package org.biobank.domain.validation

import org.biobank.domain._
import org.biobank.domain.user.User

import org.slf4j.LoggerFactory
import scalaz._
import scalaz.Scalaz._

trait UserValidationHelper extends ValidationHelper {

  val log = LoggerFactory.getLogger(this.getClass)

  val emailRegex = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?".r
  val urlRegex = "^((https?|ftp)://|(www|ftp)\\.)[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$".r

  def validateEmail(email: String): DomainValidation[String] = {
    emailRegex.findFirstIn(email) match {
      case Some(e) => email.success
      case None => s"Invalid email address: $email".failNel
    }
  }

  def validateAvatarUrl(url: Option[String]): DomainValidation[Option[String]] = {
    url match {
      case Some(url) =>
        urlRegex.findFirstIn(url) match {
          case Some(e) => some(url).success
          case None => s"invalid avatar url: $url".failNel
        }
      case None =>
        none.success
    }
  }
}
