package org.biobank.domain

import org.slf4j.LoggerFactory

import scalaz._
import scalaz.Scalaz._

trait UserRepositoryComponent {

  val userRepository: UserRepository

  /** A repository that stores [[Users]]. */
  trait UserRepository extends ReadWriteRepository[UserId, User] {

    def allUsers(): Set[User]

    def userWithId(userId: UserId): DomainValidation[User]

    def emailAvailable(email: String): DomainValidation[Boolean]

  }
}

trait UserRepositoryComponentImpl extends UserRepositoryComponent {

  val userRepository: UserRepository = new UserRepositoryImpl

  /** An implementation of repository that stores [[Users]].
    *
    * This repository uses the [[ReadWriteRepository]] implementation.
    */
  class UserRepositoryImpl extends ReadWriteRepositoryRefImpl[UserId, User](v => v.id) with UserRepository {

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
  }
}
