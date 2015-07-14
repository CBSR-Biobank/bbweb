package org.biobank.domain.user

import org.biobank.domain.{ DomainValidation, DomainError, ReadWriteRepository, ReadWriteRepositoryRefImpl }

import org.slf4j.LoggerFactory

import javax.inject.Singleton
import com.google.inject.ImplementedBy
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

/** A repository that stores [[User]]s. */
@ImplementedBy(classOf[UserRepositoryImpl])
trait UserRepository extends ReadWriteRepository[UserId, User] {

  def allUsers(): Set[User]

  def getRegistered(id: UserId): DomainValidation[RegisteredUser]

  def getActive(id: UserId): DomainValidation[ActiveUser]

  def getLocked(id: UserId): DomainValidation[LockedUser]

  def getByEmail(email: String): DomainValidation[User]

}

/** An implementation of repository that stores [[User]]s.
  *
  * This repository uses the [[ReadWriteRepository]] implementation.
 */
@Singleton
class UserRepositoryImpl
    extends ReadWriteRepositoryRefImpl[UserId, User](v => v.id)
    with UserRepository {

  override val NotFoundError = "user with id not found:"

  def nextIdentity: UserId = new UserId(nextIdentityAsString)

  def allUsers(): Set[User] = getValues.toSet

  def getRegistered(id: UserId) = {
    for {
      user       <- getByKey(id)
      registered <- user match {
        case u: RegisteredUser => u.success
        case _ => DomainError(s"user is not registered: $id").failureNel
      }
    } yield registered
  }

  def getActive(id: UserId) = {
    for {
      user   <- getByKey(id)
      active <- user match {
        case u: ActiveUser => u.success
        case _ => DomainError(s"user is not active: $id").failureNel
      }
    } yield active
  }

  def getLocked(id: UserId) = {
    for {
      user   <- getByKey(id)
      locked <- user match {
        case u: LockedUser => u.success
        case _ => DomainError(s"user is not locked: $id").failureNel
      }
    } yield locked
  }

  def getByEmail(email: String): DomainValidation[User] = {
    getValues.find(_.email == email).fold {
      DomainError(s"user with email not found: $email: ${getValues.size}").failureNel[User]
    } { user =>
      user.success
    }
  }
}
