package domain

import scalaz._
import scalaz.Scalaz._

sealed abstract class User extends ConcurrencySafeEntity[UserId] {
  def name: String
  def email: String
  def password: String

  /**
   * Authenticate a User.
   */
  def authenticate(email: String, password: String): DomainValidation[User] =
    if (this.password.equals(password)) this.success
    else DomainError("authentication failure").fail
}

object User {

  def add(name: String, email: String, password: String): UnauthenticatedUser =
    UnauthenticatedUser(UserIdentityService.nextIdentity, version = 0L, name, email, password)

}

case class UnauthenticatedUser(
  id: UserId,
  version: Long = -1,
  name: String,
  email: String,
  password: String) extends User {

}

case class AuthenticatedUser(
  id: UserId,
  version: Long = -1,
  name: String,
  email: String,
  password: String) extends User {

}
