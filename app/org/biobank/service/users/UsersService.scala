package org.biobank.service.users

import org.biobank.service._
import org.biobank.domain._
import org.biobank.domain.user._
import org.biobank.infrastructure.command.UserCommands._
import org.biobank.infrastructure.event.UserEvents._

import akka.actor.{ ActorSystem, ActorRef }
import akka.pattern.ask
import akka.util.Timeout
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scaldi.akka.AkkaInjectable
import scaldi.{Injectable, Injector}

import scalaz._
import scalaz.Scalaz._

class UsersService(implicit inj: Injector)
    extends ApplicationService
    with AkkaInjectable {

  val Log = LoggerFactory.getLogger(this.getClass)

  implicit val system = inject [ActorSystem]

  implicit val timeout = inject [Timeout] ('akkaTimeout)

  val usersProcessor = injectActorRef [UsersProcessor]

  val userRepository = inject [UserRepository]

  val passwordHasher = inject [PasswordHasher]

  def getAll: Set[User] = {
    userRepository.allUsers
  }

  def getUser(id: String): DomainValidation[User] = {
    userRepository.getByKey(UserId(id))
  }

  def getByEmail(email: String): DomainValidation[User] = {
    userRepository.getByEmail(email)
  }

  def validatePassword(email: String, enteredPwd: String): DomainValidation[User] = {
    for {
      user <- userRepository.getByEmail(email)
      validPwd <- {
        if (passwordHasher.valid(user.password, user.salt, enteredPwd)) {
          user.success
        } else {
          DomainError("invalid password").failNel
        }
      }
      notLocked <- UserHelper.isUserNotLocked(user)
    } yield user
  }

  def resetPassword(cmd: ResetUserPasswordCmd)
      : Future[DomainValidation[UserPasswordResetEvent]] = {
    ask(usersProcessor, cmd).map (
      _.asInstanceOf[DomainValidation[UserPasswordResetEvent]])
  }

  def register(cmd: RegisterUserCmd): Future[DomainValidation[UserRegisteredEvent]] = {
    ask(usersProcessor, cmd).map (
      _.asInstanceOf[DomainValidation[UserRegisteredEvent]])
  }

  def updateName(cmd: UpdateUserNameCmd)(implicit userId: UserId)
      : Future[DomainValidation[UserNameUpdatedEvent]] = {
    ask(usersProcessor, cmd, userId).map (_.asInstanceOf[DomainValidation[UserNameUpdatedEvent]])
  }

  def updateEmail(cmd: UpdateUserEmailCmd)(implicit userId: UserId)
      : Future[DomainValidation[UserEmailUpdatedEvent]] = {
    ask(usersProcessor, cmd, userId).map (_.asInstanceOf[DomainValidation[UserEmailUpdatedEvent]])
  }

  def updatePassword(cmd: UpdateUserPasswordCmd)(implicit userId: UserId)
      : Future[DomainValidation[UserPasswordUpdatedEvent]] = {
    ask(usersProcessor, cmd, userId).map (_.asInstanceOf[DomainValidation[UserPasswordUpdatedEvent]])
  }

  def activate(cmd: ActivateUserCmd)(implicit userId: UserId)
      : Future[DomainValidation[UserActivatedEvent]] = {
    ask(usersProcessor, cmd, userId).map (_.asInstanceOf[DomainValidation[UserActivatedEvent]])
  }

  def lock(cmd: LockUserCmd)(implicit userId: UserId)
      : Future[DomainValidation[UserLockedEvent]] = {
    ask(usersProcessor, cmd, userId).map (_.asInstanceOf[DomainValidation[UserLockedEvent]])
  }

  def unlock(cmd: UnlockUserCmd)(implicit userId: UserId)
      : Future[DomainValidation[UserUnlockedEvent]] = {
    ask(usersProcessor, cmd, userId).map (_.asInstanceOf[DomainValidation[UserUnlockedEvent]])
  }

  def remove(cmd: RemoveUserCmd)(implicit userId: UserId)
      : Future[DomainValidation[UserRemovedEvent]] = {
    ask(usersProcessor, cmd, userId).map (_.asInstanceOf[DomainValidation[UserRemovedEvent]])
  }

}

