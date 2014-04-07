package domain

import org.slf4j.LoggerFactory

import scalaz._
import Scalaz._

trait UserRepositoryComponent {

  val userRepository: UserRepository

  trait UserRepository {

    def allUsers(): Set[User]

    def userWithId(userId: UserId): Option[User]

    def emailAvailable(email: String): Boolean

    def add(user: RegisteredUser): RegisteredUser

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

    def userWithId(userId: UserId): Option[User] = getByKey(userId)

    def emailAvailable(email: String): Boolean = {
      getByKey(new UserId(email)) match {
        case None => true
        case Some(user) => false
      }
    }

    def add(user: RegisteredUser): RegisteredUser = {
      getByKey(user.id) match {
        case Some(prevItem) =>
          throw new IllegalArgumentException(s"user with ID already exists: ${user.id}")

        case None =>
          updateMap(user)
          user
      }
    }

    def update(user: User): DomainValidation[User] = {
      getByKey(user.id) match {
        case None =>
          throw new IllegalArgumentException(s"user does not exist: { userId: ${user.id} }")
        case Some(prevUser) =>
          for {
            validVersion <- prevUser.requireVersion(Some(user.version))
            updatedItem <- updateMap(user).success
          } yield user
      }
    }
  }
}
