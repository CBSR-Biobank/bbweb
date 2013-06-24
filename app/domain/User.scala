package domain

import infrastructure._

import scalaz._
import scalaz.Scalaz._

sealed abstract class User extends ConcurrencySafeEntity[UserId] {
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
    else DomainError("authentication failure").fail
}

object User {

  def add(
    id: UserId,
    name: String,
    email: String,
    password: String,
    hasher: String,
    salt: Option[String],
    avatarUrl: Option[String]): DomainValidation[RegisteredUser] =
    RegisteredUser(id, version = 0L, name, email, password, hasher, salt, avatarUrl,
      addedBy = null, timeAdded = -1, updatedBy = None, timeUpdated = None).success

}

case class RegisteredUser(
  id: UserId,
  version: Long = -1,
  name: String,
  email: String,
  password: String,
  hasher: String,
  salt: Option[String],
  avatarUrl: Option[String],
  addedBy: UserId = null,
  timeAdded: Long = -1,
  updatedBy: Option[UserId] = None,
  timeUpdated: Option[Long] = None) extends User {

}

case class ActiveUser(
  id: UserId,
  version: Long = -1,
  name: String,
  email: String,
  password: String,
  hasher: String,
  salt: Option[String],
  avatarUrl: Option[String],
  addedBy: UserId = null,
  timeAdded: Long = -1,
  updatedBy: Option[UserId] = None,
  timeUpdated: Option[Long] = None) extends User {

}

case class LockedUser(
  id: UserId,
  version: Long = -1,
  name: String,
  email: String,
  password: String,
  hasher: String,
  salt: Option[String],
  avatarUrl: Option[String],
  addedBy: UserId = null,
  timeAdded: Long = -1,
  updatedBy: Option[UserId] = None,
  timeUpdated: Option[Long] = None) extends User {

}
