package domain

import scalaz._
import scalaz.Scalaz._

sealed abstract class User extends Entity[UserId] {
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
  val invalidVersionMessage = "user %s: expected version %s doesn't match current version %s"

  def invalidVersion(userId: Identity, expected: Long, current: Long) =
    DomainError(invalidVersionMessage format (userId, expected, current))

  def requireVersion[T <: User](user: T, expectedVersion: Option[Long]): DomainValidation[T] = {
    val id = user.id
    val version = user.version

    expectedVersion match {
      case Some(expected) if (version != expected) => invalidVersion(id, expected, version).fail
      case Some(expected) if (version == expected) => user.success
      case None => user.success
    }
  }

  // TODO: not sure yet if this is the right place for this method
  def nextIdentity: UserId =
    new UserId(java.util.UUID.randomUUID.toString.toUpperCase)

  def add(id: UserId, name: String, email: String, password: String): DomainValidation[UnauthenticatedUser] =
    UnauthenticatedUser(id, version = 0L, name, email, password).success
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
