package domain

import org.slf4j.LoggerFactory

import scalaz._
import Scalaz._

trait UserRepositoryComponent {

  val userRepository: UserRepository

  trait UserRepository {

    def allUsers(): Set[User]

    def userWithId(userId: UserId): DomainValidation[User]

    def add(user: RegisteredUser): DomainValidation[RegisteredUser]

    def update(user: User): DomainValidation[User]

  }
}

trait UserRepositoryComponentImpl extends UserRepositoryComponent {

  val userRepository: UserRepository = new UserRepositoryImpl

  class UserRepositoryImpl extends ReadWriteRepository[UserId, User](v => v.id) with UserRepository {

    val log = LoggerFactory.getLogger(this.getClass)

    def allUsers(): Set[User] = {
      getValues.toSet
    }

    def userWithId(userId: UserId): DomainValidation[User] = {
      getByKey(userId) match {
        case Failure(x) => DomainError("user does not exist: { userId: %s}".format(userId)).fail
        case Success(user) =>
          user.success
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
}
