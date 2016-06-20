package org.biobank.domain.user

import org.biobank.domain.{ DomainValidation, ReadWriteRepository, ReadWriteRepositoryRefImpl }

import javax.inject.Singleton
import com.google.inject.ImplementedBy
import scalaz._
import scalaz.Scalaz._

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
    getByKey(id) match {
      case Success(u: RegisteredUser) => u.success
      case Success(u) => InvalidStatus(s"user is not registered: $id").failureNel
      case Failure(err) => err.failure[RegisteredUser]
    }
  }

  def getActive(id: UserId) = {
    getByKey(id) match {
      case Success(u: ActiveUser) => u.success
      case Success(u) => InvalidStatus(s"user is not active: $id").failureNel
      case Failure(err) => err.failure[ActiveUser]
    }
  }

  def getLocked(id: UserId) = {
    getByKey(id) match {
      case Success(u: LockedUser) => u.success
      case Success(u) => InvalidStatus(s"user is not active: $id").failureNel
      case Failure(err) => err.failure[LockedUser]
    }
  }

  def getByEmail(email: String): DomainValidation[User] = {
    getValues.find(_.email == email)
      .toSuccessNel(EmailNotFound(s"user email not found: $email").toString)
  }
}
