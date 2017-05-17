package org.biobank.domain.user

import org.biobank.domain.Factory
import org.biobank.service.PasswordHasher

trait UserFixtures {

  class UsersOfAllStates(val registeredUser: RegisteredUser,
                         val activeUser:     ActiveUser,
                         val lockedUser:     LockedUser)

  def passwordHasher: PasswordHasher

  val factory: Factory

  def createRegisteredUser(plainPassword: String): RegisteredUser = {
    val salt = passwordHasher.generateSalt

    factory.createRegisteredUser.copy(
      salt = salt,
      password = passwordHasher.encrypt(plainPassword, salt))
  }

  def createActiveUser(plainPassword: String): ActiveUser = {
    val salt = passwordHasher.generateSalt

    factory.createActiveUser.copy(
      salt = salt,
      password = passwordHasher.encrypt(plainPassword, salt))
  }

  def createLockedUser(plainPassword: String): LockedUser = {
    val salt = passwordHasher.generateSalt

    factory.createLockedUser.copy(
      salt = salt,
      password = passwordHasher.encrypt(plainPassword, salt))
  }

  def usersOfAllStates() =
    new UsersOfAllStates(factory.createRegisteredUser,
                         factory.createActiveUser,
                         factory.createLockedUser)
}
