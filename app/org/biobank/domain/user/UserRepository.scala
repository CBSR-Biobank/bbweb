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

    def nextIdentity: UserId = new UserId(nextIdentityAsString)

    def allUsers(): Set[User] = getValues.toSet

  }
}
