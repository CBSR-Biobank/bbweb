package org.biobank.service.users

import org.biobank.service._
import org.biobank.dto._
import org.biobank.domain._
import org.biobank.domain.user._
import org.biobank.infrastructure._
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
import scalaz.Validation.FlatMap._

class UsersService(implicit inj: Injector) extends AkkaInjectable {

  val Log = LoggerFactory.getLogger(this.getClass)

  implicit val system = inject [ActorSystem]

  implicit val timeout = inject [Timeout] ('akkaTimeout)

  val usersProcessor = injectActorRef [UsersProcessor] ("user")

  val userRepository = inject [UserRepository]

  val passwordHasher = inject [PasswordHasher]

  def getAll: Set[User] = {
    userRepository.allUsers
  }

  def getCountsByStatus(): UserCountsByStatus = {
    // FIXME should be replaced by DTO query to the database
    val users = userRepository.getValues
      UserCountsByStatus(
        total           = users.size,
        registeredCount = users.collect { case u: RegisteredUser => u }.size,
        activeCount     = users.collect { case u: ActiveUser     => u }.size,
        lockedCount     = users.collect { case u: LockedUser     => u }.size
      )
  }

  private def getStatus(status: String): DomainValidation[String] = {
    status match {
      case "all"        => User.status.successNel
      case "registered" => RegisteredUser.status.successNel
      case "active"     => ActiveUser.status.successNel
      case "locked"     => LockedUser.status.successNel
      case _            => DomainError(s"invalid user status: $status").failureNel
    }
  }

  def getUsers[T <: User]
    (nameFilter: String,
      emailFilter: String,
      status: String,
      sortFunc: (User, User) => Boolean,
      order: SortOrder)
      : DomainValidation[Seq[User]] = {
    val allUsers = userRepository.getValues

    val usersFilteredByName = if (!nameFilter.isEmpty) {
      val nameFilterLowerCase = nameFilter.toLowerCase
      allUsers.filter { _.name.toLowerCase.contains(nameFilterLowerCase) }
    } else {
      allUsers
    }

    val usersFilteredByEmail = if (!emailFilter.isEmpty) {
      val emailFilterLowerCase = emailFilter.toLowerCase
      usersFilteredByName.filter { _.email.toLowerCase.contains(emailFilterLowerCase) }
    } else {
      usersFilteredByName
    }

    val usersFilteredByStatus = getStatus(status).map { status =>
      if (status == User.status) {
        usersFilteredByEmail
      } else {
        usersFilteredByEmail.filter { user => user.status == status }
      }
    }

    usersFilteredByStatus.map { users =>
      val result = users.toSeq.sortWith(sortFunc)

      if (order == AscendingOrder) {
        result
      } else {
        result.reverse
      }
    }
  }

  def getUser(id: String): DomainValidation[User] = {
    userRepository.getByKey(UserId(id)).fold(
      err => DomainError(s"user with id does not exist: $id").failureNel,
      user => user.success
    )
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
          DomainError("invalid password").failureNel
        }
      }
      notLocked <- UserHelper.isUserNotLocked(user)
    } yield user
  }

  def resetPassword(cmd: ResetUserPasswordCmd)
      : Future[DomainValidation[User]] = {
    replyWithUser(ask(usersProcessor, cmd).mapTo[DomainValidation[UserEvent]])
  }

  def register(cmd: RegisterUserCmd): Future[DomainValidation[User]] = {
    replyWithUser(ask(usersProcessor, cmd).mapTo[DomainValidation[UserEvent]])
  }

  def updateName(cmd: UpdateUserNameCmd)(implicit userId: UserId)
      : Future[DomainValidation[User]] = {
    replyWithUser(ask(usersProcessor, cmd).mapTo[DomainValidation[UserEvent]])
  }

  def updateEmail(cmd: UpdateUserEmailCmd)(implicit userId: UserId)
      : Future[DomainValidation[User]] = {
    replyWithUser(ask(usersProcessor, cmd).mapTo[DomainValidation[UserEvent]])
  }

  def updatePassword(cmd: UpdateUserPasswordCmd)(implicit userId: UserId)
      : Future[DomainValidation[User]] = {
    replyWithUser(ask(usersProcessor, cmd).mapTo[DomainValidation[UserEvent]])
  }

  def updateAvatarUrl(cmd: UpdateUserAvatarUrlCmd)(implicit userId: UserId)
      : Future[DomainValidation[User]] = {
    replyWithUser(ask(usersProcessor, cmd).mapTo[DomainValidation[UserEvent]])
  }

  def activate(cmd: ActivateUserCmd)(implicit userId: UserId)
      : Future[DomainValidation[User]] = {
    replyWithUser(ask(usersProcessor, cmd).mapTo[DomainValidation[UserEvent]])
  }

  def lock(cmd: LockUserCmd)(implicit userId: UserId)
      : Future[DomainValidation[User]] = {
    replyWithUser(ask(usersProcessor, cmd).mapTo[DomainValidation[UserEvent]])
  }

  def unlock(cmd: UnlockUserCmd)(implicit userId: UserId)
      : Future[DomainValidation[User]] = {
    replyWithUser(ask(usersProcessor, cmd).mapTo[DomainValidation[UserEvent]])
  }

  private def replyWithUser(future: Future[DomainValidation[UserEvent]])
      : Future[DomainValidation[User]] = {
    future map { validation =>
      for {
        event <- validation
        user <- userRepository.getByKey(UserId(event.id))
      } yield user
    }
  }

}

