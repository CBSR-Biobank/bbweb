package org.biobank.domain

import org.slf4j.LoggerFactory

import scalaz._
import scalaz.Scalaz._

trait UserRepositoryComponent {

  val userRepository: UserRepository

  /** A repository that stores [[Users]]. */
  trait UserRepository {

    def allUsers(): Set[User]

    def userWithId(userId: UserId): DomainValidation[User]

    def emailAvailable(email: String): DomainValidation[Boolean]

    def add(user: RegisteredUser): RegisteredUser

    def update(user: User): DomainValidation[User]

  }
}

trait UserRepositoryComponentImpl extends UserRepositoryComponent {

  val userRepository: UserRepository = new UserRepositoryImpl

  /** An implementation of repository that stores [[Users]].
    *
    * This repository uses the [[ReadWriteRepository]] implementation.
    */
  class UserRepositoryImpl extends ReadWriteRepository[UserId, User](v => v.id) with UserRepository {

    val log = LoggerFactory.getLogger(this.getClass)

    def allUsers(): Set[User] = {
      getValues.toSet
    }

    def userWithId(userId: UserId): DomainValidation[User] = getByKey(userId)

    def emailAvailable(email: String): DomainValidation[Boolean] = {
      getByKey(new UserId(email)) match {
        case Failure(err) => true.success
        case Success(user) => DomainError(s"user already exists: $email").failNel
      }
    }

    def add(user: RegisteredUser): RegisteredUser = {
      getByKey(user.id) match {
        case Success(prevItem) =>
          throw new IllegalArgumentException(s"user with ID already exists: ${user.id}")

        case Failure(err) =>
          updateMap(user)
          user
      }
    }

    def update(user: User): DomainValidation[User] = {
      getByKey(user.id) match {
        case Failure(err) =>
          throw new IllegalArgumentException(s"user does not exist: { userId: ${user.id} }")
        case Success(prevUser) =>
          for {
            validVersion <- prevUser.requireVersion(Some(user.version))
            updatedItem <- updateMap(user).success
          } yield user
      }
    }
  }
}
