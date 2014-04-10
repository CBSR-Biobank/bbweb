package domain

import domain.validator.UserValidator
import infrastructure.command.UserCommands._

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
  def authenticate(email: String, password: String): DomainValidation[User] =
    if (this.password.equals(password)) this.success
    else DomainError("authentication failure").failNel
}

object User {

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

}

object RegisteredUser extends UserValidator {

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

  def create(cmd: AddUserCommand): DomainValidation[RegisteredUser] = {
    create(UserId(cmd.email), 0L, cmd.name, cmd.email, cmd.password, cmd.hasher, cmd.salt,
      cmd.avatarUrl)
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

  def lock(
    id: UserId,
    version: Long,
    name: String,
    email: String,
    password: String,
    hasher: String,
    salt: Option[String],
    avatarUrl: Option[String]): DomainValidation[LockedUser] = {
    LockedUser.create(id, version, name, email, password, hasher, salt, avatarUrl)
  }

}

object ActiveUser extends UserValidator {

  def create(
    id: UserId,
    version: Long,
    name: String,
    email: String,
    password: String,
    hasher: String,
    salt: Option[String],
    avatarUrl: Option[String]): DomainValidation[ActiveUser] = {
    (validateId(id).toValidationNel |@|
      validateAndIncrementVersion(version).toValidationNel |@|
      validateNonEmpty("name", name).toValidationNel |@|
      validateEmail(email).toValidationNel |@|
      validateNonEmpty("password", password).toValidationNel |@|
      validateNonEmpty("hasher", hasher).toValidationNel |@|
      validateNonEmptyOption("salt", salt).toValidationNel |@|
      validateAvatarUrl(avatarUrl).toValidationNel) {
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

  def unlock(
    id: UserId,
    version: Long,
    name: String,
    email: String,
    password: String,
    hasher: String,
    salt: Option[String],
    avatarUrl: Option[String]): DomainValidation[ActiveUser] = {
    ActiveUser.create(id, version, name, email, password, hasher, salt, avatarUrl)
  }

}

object LockedUser extends UserValidator {

  def create(
    id: UserId,
    version: Long,
    name: String,
    email: String,
    password: String,
    hasher: String,
    salt: Option[String],
    avatarUrl: Option[String]): DomainValidation[LockedUser] = {
    (validateId(id).toValidationNel |@|
      validateAndIncrementVersion(version).toValidationNel |@|
      validateNonEmpty("name", name).toValidationNel |@|
      validateEmail(email).toValidationNel |@|
      validateNonEmpty("password", password).toValidationNel |@|
      validateNonEmpty("hasher", hasher).toValidationNel |@|
      validateNonEmptyOption("salt", salt).toValidationNel |@|
      validateAvatarUrl(avatarUrl).toValidationNel) {
        LockedUser(_, _, _, _, _, _, _, _)
      }
  }

}
