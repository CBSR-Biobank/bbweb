package domain

import infrastructure._

import scalaz._
import scalaz.Scalaz._

sealed abstract class User extends ConcurrencySafeEntity[UserId] {
  def name: String
  def email: String
  def password: String
  def hasher: String
  def salt: Option[String]
  def avatarUrl: Option[String]

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
    RegisteredUser(id, version = 0L, name, email, password, hasher, salt, avatarUrl).success

}

case class RegisteredUser(
  id: UserId,
  version: Long = -1,
  name: String,
  email: String,
  password: String,
  hasher: String,
  salt: Option[String],
  avatarUrl: Option[String]) extends User {

}

case class ActiveUser(
  id: UserId,
  version: Long = -1,
  name: String,
  email: String,
  password: String,
  hasher: String,
  salt: Option[String],
  avatarUrl: Option[String]) extends User {

}

case class LockedUser(
  id: UserId,
  version: Long = -1,
  name: String,
  email: String,
  password: String,
  hasher: String,
  salt: Option[String],
  avatarUrl: Option[String]) extends User {

}
