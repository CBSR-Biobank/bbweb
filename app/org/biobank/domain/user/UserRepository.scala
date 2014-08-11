package org.biobank.domain.user

import org.biobank.domain.{ DomainValidation, DomainError, ReadWriteRepository, ReadWriteRepositoryRefImpl }

import org.slf4j.LoggerFactory

import scalaz._
import scalaz.Scalaz._

trait UserRepositoryComponent {

  val userRepository: UserRepository

  /** A repository that stores [[User]]s. */
  trait UserRepository extends ReadWriteRepository[UserId, User] {

    def allUsers(): Set[User]

    def emailAvailable(email: String): DomainValidation[Boolean]

  }
}

trait UserRepositoryComponentImpl extends UserRepositoryComponent {

  val userRepository: UserRepository = new UserRepositoryImpl

  /** An implementation of repository that stores [[User]]s.
    *
    * This repository uses the [[ReadWriteRepository]] implementation.
    */
  class UserRepositoryImpl
      extends ReadWriteRepositoryRefImpl[UserId, User](v => v.id)
      with UserRepository {

    // the email address is used for identity in this repository
    def nextIdentity: UserId = throw new IllegalStateException("should not be used")

    def allUsers(): Set[User] = getValues.toSet

    def emailAvailable(email: String): DomainValidation[Boolean] = {
      getByKey(new UserId(email)).fold(
        err => true.success,
        user => DomainError(s"user already exists: $email").failNel
      )
    }
  }
}
