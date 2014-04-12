package domain

import domain.validator.UserValidator
import infrastructure.event.UserEvents._

import org.slf4j.LoggerFactory
import scalaz._
import scalaz.Scalaz._

sealed trait User extends ConcurrencySafeEntity[UserId] {
  val name: String
  val email: String
  val password: String
  val hasher: String
  val salt: Option[String]
  val avatarUrl: Option[String]

  /**
   * Authenticate a User.
   */
  def authenticate(email: String, password: String): DomainValidation[User] = {
    if (this.password.equals(password)) this.success
    else DomainError("authentication failure").failNel
  }
}

case class RegisteredUser private (
  id: UserId,
  version: Long = -1,
  name: String,
  email: String,
  password: String,
  hasher: String,
  salt: Option[String],
  avatarUrl: Option[String]) extends User {

  def activate: DomainValidation[ActiveUser] = {
    ActiveUser.create(this)
  }
}

object RegisteredUser extends UserValidator {

  override val log = LoggerFactory.getLogger(this.getClass)

  def create(
    id: UserId,
    version: Long,
    name: String,
    email: String,
    password: String,
    hasher: String,
    salt: Option[String],
    avatarUrl: Option[String]): DomainValidation[RegisteredUser] = {
    (validateId(id).toValidationNel |@|
      validateAndIncrementVersion(version).toValidationNel |@|
      validateNonEmpty("name", name).toValidationNel |@|
      validateEmail(email).toValidationNel |@|
      validateNonEmpty("password", password).toValidationNel |@|
      validateNonEmpty("hasher", hasher).toValidationNel |@|
      validateNonEmptyOption("salt", salt).toValidationNel |@|
      validateAvatarUrl(avatarUrl).toValidationNel) {
        RegisteredUser(_, _, _, _, _, _, _, _)
      }
  }

}

case class ActiveUser private (
  id: UserId,
  version: Long = -1,
  name: String,
  email: String,
  password: String,
  hasher: String,
  salt: Option[String],
  avatarUrl: Option[String]) extends User {

  def lock: DomainValidation[LockedUser] = {
    LockedUser.create(this)
  }
}

object ActiveUser extends UserValidator {

  def create[T <: User](user: T): DomainValidation[ActiveUser] = {
    (validateId(user.id).toValidationNel |@|
      validateAndIncrementVersion(user.version).toValidationNel |@|
      validateNonEmpty("name", user.name).toValidationNel |@|
      validateEmail(user.email).toValidationNel |@|
      validateNonEmpty("password", user.password).toValidationNel |@|
      validateNonEmpty("hasher", user.hasher).toValidationNel |@|
      validateNonEmptyOption("salt", user.salt).toValidationNel |@|
      validateAvatarUrl(user.avatarUrl).toValidationNel) {
        ActiveUser(_, _, _, _, _, _, _, _)
      }
  }

}

case class LockedUser private (
  id: UserId,
  version: Long = -1,
  name: String,
  email: String,
  password: String,
  hasher: String,
  salt: Option[String],
  avatarUrl: Option[String]) extends User {

  def unlock: DomainValidation[ActiveUser] = {
    ActiveUser.create(this)
  }

}

object LockedUser extends UserValidator {

  def create(user: ActiveUser): DomainValidation[LockedUser] = {
    (validateId(user.id).toValidationNel |@|
      validateAndIncrementVersion(user.version).toValidationNel |@|
      validateNonEmpty("name", user.name).toValidationNel |@|
      validateEmail(user.email).toValidationNel |@|
      validateNonEmpty("password", user.password).toValidationNel |@|
      validateNonEmpty("hasher", user.hasher).toValidationNel |@|
      validateNonEmptyOption("salt", user.salt).toValidationNel |@|
      validateAvatarUrl(user.avatarUrl).toValidationNel) {
        LockedUser(_, _, _, _, _, _, _, _)
      }
  }

}
