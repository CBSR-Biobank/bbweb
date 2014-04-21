package org.biobank.domain

import org.biobank.domain.validation.UserValidationHelper
import org.biobank.infrastructure.event.UserEvents._

import scalaz._
import scalaz.Scalaz._

/** A user of the system.
  */
sealed trait User extends ConcurrencySafeEntity[UserId] {

  /** The user's full name. */
  val name: String

  /** The user's email. Must be unique to the system. */
  val email: String

  /** The user's password */
  val password: String

  /** The string used to hash the password. */
  val hasher: String

  /** The string used to salt the password. */
  val salt: Option[String]

  /** An optional URL to the user's avatar icon. */
  val avatarUrl: Option[String]

  /**
   * Authenticate a user.
   */
  def authenticate(email: String, password: String): DomainValidation[User] = {
    if (this.password.equals(password)) this.success
    else DomainError("authentication failure").failNel
  }

  override def toString =
    s"""|${this.getClass.getSimpleName}: {
        |  id: $id,
        |  version: $version,
        |  name: $name,
        |  email: $email
        |}""".stripMargin
}

/** A user that just registered with the system. This user does not yet have full access
  * the system.
  */
case class RegisteredUser private (
  id: UserId,
  version: Long,
  name: String,
  email: String,
  password: String,
  hasher: String,
  salt: Option[String],
  avatarUrl: Option[String]) extends User {

  /* Activates a registered user. */
  def activate(expectedVersion: Option[Long]): DomainValidation[ActiveUser] = {
    for {
      validVersion <- requireVersion(expectedVersion)
      activatedUser <- ActiveUser.create(this)
    } yield activatedUser
  }
}

/** Factory object. */
object RegisteredUser extends UserValidationHelper {

  /** Creates a registered user. */
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

/** A user that has access to the system. */
case class ActiveUser private (
  id: UserId,
  version: Long = -1,
  name: String,
  email: String,
  password: String,
  hasher: String,
  salt: Option[String],
  avatarUrl: Option[String]) extends User {

  /** Locks an active user. */
  def lock(expectedVersion: Option[Long]): DomainValidation[LockedUser] = {
    for {
      validVersion <- requireVersion(expectedVersion)
      lockedUser <- LockedUser.create(this)
    } yield lockedUser
  }

  // FIXME: add update method
}

/** Factory object. */
object ActiveUser extends UserValidationHelper {

  /** Creates an active user from a registered user. */
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

/** A user who no longer has access to the system. */
case class LockedUser private (
  id: UserId,
  version: Long = -1,
  name: String,
  email: String,
  password: String,
  hasher: String,
  salt: Option[String],
  avatarUrl: Option[String]) extends User {

  /** Unlocks a locked user. */
  def unlock(expectedVersion: Option[Long]): DomainValidation[ActiveUser] = {
    for {
      validVersion <- requireVersion(expectedVersion)
      activeUser <- ActiveUser.create(this)
    } yield activeUser
  }

}

/** Factory object. */
object LockedUser extends UserValidationHelper {

  /** Creates an active user from a locked user. */
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

