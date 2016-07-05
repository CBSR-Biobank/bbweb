package org.biobank.domain.user

import org.biobank.domain.{ DomainValidation, ReadWriteRepository, ReadWriteRepositoryRefImpl }

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
  import org.biobank.CommonValidations._

  def nextIdentity: UserId = new UserId(nextIdentityAsString)

  def notFound(id: UserId) = IdNotFound(s"user id: $id")

  def allUsers(): Set[User] = getValues.toSet

  override def getByKey(id: UserId): DomainValidation[User] = {
    getMap.get(id).toSuccessNel(notFound(id).toString)
  }

  def getRegistered(id: UserId): DomainValidation[RegisteredUser] = {
    for {
      user <- getByKey(id)
      registered <- {
        user match {
          case u: RegisteredUser => u.successNel[String]
          case u => InvalidStatus(s"user is not registered: $id").failureNel[RegisteredUser]
        }
      }
    } yield registered
  }

  def getActive(id: UserId) = {
    for {
      user <- getByKey(id)
      active <- {
        user match {
          case u: ActiveUser => u.successNel[String]
          case u => InvalidStatus(s"user is not active: $id").failureNel[ActiveUser]
        }
      }
    } yield active
  }

  def getLocked(id: UserId) = {
    for {
      user <- getByKey(id)
      locked <- {
        user match {
          case u: LockedUser => u.successNel[String]
          case u => InvalidStatus(s"user is not active: $id").failureNel[LockedUser]
        }
      }
    } yield locked
  }

  def getByEmail(email: String): DomainValidation[User] = {
    getValues.find(_.email == email)
      .toSuccess(EmailNotFound(s"user email not found: $email").nel)
  }
}
