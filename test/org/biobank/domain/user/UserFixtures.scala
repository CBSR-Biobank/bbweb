package org.biobank.domain.users

import org.biobank.domain.Factory
import org.biobank.services.PasswordHasher

trait UserFixtures {

  class UsersOfAllStates(val registeredUser: RegisteredUser,
                         val activeUser:     ActiveUser,
                         val lockedUser:     LockedUser)

  protected def passwordHasher: PasswordHasher

  protected val factory: Factory

  protected def createRegisteredUser(plainPassword: String): RegisteredUser = {
    val salt = passwordHasher.generateSalt

    factory.createRegisteredUser.copy(
      salt = salt,
      password = passwordHasher.encrypt(plainPassword, salt))
  }

  protected def createActiveUser(plainPassword: String): ActiveUser = {
    val salt = passwordHasher.generateSalt

    factory.createActiveUser.copy(
      salt = salt,
      password = passwordHasher.encrypt(plainPassword, salt))
  }

  protected def createLockedUser(plainPassword: String): LockedUser = {
    val salt = passwordHasher.generateSalt

    factory.createLockedUser.copy(
      salt = salt,
      password = passwordHasher.encrypt(plainPassword, salt))
  }

  protected def usersOfAllStates() =
    new UsersOfAllStates(factory.createRegisteredUser,
                         factory.createActiveUser,
                         factory.createLockedUser)
}
