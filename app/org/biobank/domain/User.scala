package org.biobank.domain

import org.biobank.domain.validation.UserValidationHelper
import org.biobank.infrastructure.event.UserEvents._

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

  override def toString =
    s"""|User: {
        |  name: $name,
        |  email: $email
        |}""".stripMargin
}

case class RegisteredUser private (
  id: UserId,
  version: Long,
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

object RegisteredUser extends UserValidationHelper {

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
      validateNonEmpty(name, "name is null or empty").toValidationNel |@|
      validateEmail(email).toValidationNel |@|
      validateNonEmpty(password, "password is null or empty").toValidationNel |@|
      validateNonEmpty(hasher, "hasher is null or empty").toValidationNel |@|
      validateNonEmptyOption(salt, "salt is null or empty").toValidationNel |@|
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

object ActiveUser extends UserValidationHelper {

  def create[T <: User](user: T): DomainValidation[ActiveUser] = {
    (validateId(user.id).toValidationNel |@|
      validateAndIncrementVersion(user.version).toValidationNel |@|
      validateNonEmpty(user.name, "name is null or empty").toValidationNel |@|
      validateEmail(user.email).toValidationNel |@|
      validateNonEmpty(user.password, "password is null or empty").toValidationNel |@|
      validateNonEmpty(user.hasher, "hasher is null or empty").toValidationNel |@|
      validateNonEmptyOption(user.salt, "salt is null or empty").toValidationNel |@|
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

object LockedUser extends UserValidationHelper {

  def create(user: ActiveUser): DomainValidation[LockedUser] = {
    (validateId(user.id).toValidationNel |@|
      validateAndIncrementVersion(user.version).toValidationNel |@|
      validateNonEmpty(user.name, "name is null or empty").toValidationNel |@|
      validateEmail(user.email).toValidationNel |@|
      validateNonEmpty(user.password, "password is null or empty").toValidationNel |@|
      validateNonEmpty(user.hasher, "hasher is null or empty").toValidationNel |@|
      validateNonEmptyOption(user.salt, "salt is null or empty").toValidationNel |@|
      validateAvatarUrl(user.avatarUrl).toValidationNel) {
        LockedUser(_, _, _, _, _, _, _, _)
      }
  }

}

