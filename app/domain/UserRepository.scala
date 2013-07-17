package domain

import scalaz._
import Scalaz._

object UserRepository extends ReadWriteRepository[UserId, User](v => v.id) {

  def allUsers(): Set[User] = {
    getValues.toSet
  }

  def userWithId(userId: UserId): DomainValidation[User] = {
    getByKey(userId) match {
      case Failure(x) => DomainError("user does not exist: { userId: %s}".format(userId)).fail
      case Success(user) => user.success
    }
  }

  def add(user: RegisteredUser): DomainValidation[RegisteredUser] = {
    getByKey(user.id) match {
      case Success(prevItem) =>
        DomainError("user with ID already exists: %s" format user.id).fail
      case Failure(x) =>
        updateMap(user)
        user.success
    }
  }

  def update(user: User): DomainValidation[User] = {
    for {
      prevUser <- userWithId(user.id)
      validVersion <- prevUser.requireVersion(Some(user.version))
      updatedItem <- updateMap(user).success
    } yield user
  }

}
