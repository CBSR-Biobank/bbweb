package org.biobank.service.users

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.ImplementedBy
import javax.inject._
import org.biobank.ValidationKey
import org.biobank.domain.user._
import org.biobank.dto._
import org.biobank.infrastructure.AscendingOrder
import org.biobank.infrastructure.command.UserCommands._
import org.biobank.infrastructure.event.UserEvents._
import org.biobank.service._
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future
import scala.concurrent.duration._

import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@ImplementedBy(classOf[UsersServiceImpl])
trait UsersService {

  def getAll: Set[User]

  def getCountsByStatus(): UserCountsByStatus

  /**
   * Returns a set of users. The entities can be filtered and or sorted using expressions.
   *
   * @param filter the string representation of the filter expression to use to filter the users.
   *
   * @param sort the string representation of the sort expression to use when sorting the users.
   */
  def getUsers(filter: FilterString, sort: SortString): ServiceValidation[Seq[User]]

  def getUser(id: UserId): ServiceValidation[User]

  def getByEmail(email: String): ServiceValidation[User]

  def validatePassword(email: String, enteredPwd: String): ServiceValidation[User]

  def allowLogin(user: User): ServiceValidation[Boolean]

  def register(cmd: RegisterUserCmd): Future[ServiceValidation[User]]

  def resetPassword(cmd: ResetUserPasswordCmd): Future[ServiceValidation[User]]

  def processCommand(cmd: UserCommand): Future[ServiceValidation[User]]

}

class UsersServiceImpl @javax.inject.Inject() (
  @Named("usersProcessor") val processor: ActorRef,
  val userRepository: UserRepository,
  val passwordHasher: PasswordHasher)
    extends UsersService {
  import org.biobank.CommonValidations._

  case object InvalidPassword extends ValidationKey

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  implicit val timeout: Timeout = 5.seconds

  def getAll: Set[User] = {
    userRepository.allUsers
  }

  @SuppressWarnings(Array("org.wartremover.warts.PublicInference"))
  def getCountsByStatus(): UserCountsByStatus = {
    // FIXME should be replaced by DTO query to the database
    val users = userRepository.getValues
      UserCountsByStatus(
        total           = users.size.toLong,
        registeredCount = users.collect { case u: RegisteredUser => u }.size.toLong,
        activeCount     = users.collect { case u: ActiveUser     => u }.size.toLong,
        lockedCount     = users.collect { case u: LockedUser     => u }.size.toLong
      )
  }

  def getUsers(filter: FilterString, sort: SortString): ServiceValidation[Seq[User]] = {
    val allUsers = userRepository.getValues.toSet
    val sortStr = if (sort.expression.isEmpty) new SortString("email")
                  else sort
    for {
      users           <- UserFilter.filterUsers(allUsers, filter)
      sortExpressions <- { QuerySortParser(sortStr).
                            toSuccessNel(ServiceError(s"could not parse sort expression: $sort")) }
      firstSort       <- { sortExpressions.headOption.
                            toSuccessNel(ServiceError("at least one sort expression is required")) }
      sortFunc        <- { User.sort2Compare.get(firstSort.name).
                            toSuccessNel(ServiceError(s"invalid sort field: ${firstSort.name}")) }
    } yield {
      val result = users.toSeq.sortWith(sortFunc)
      if (firstSort.order == AscendingOrder) result
      else result.reverse
    }
  }

  def getUser(id: UserId): ServiceValidation[User] = {
    userRepository.getByKey(id)
      .leftMap(_ => IdNotFound(s"user with id does not exist: $id").nel)
  }

  def getByEmail(email: String): ServiceValidation[User] = {
    userRepository.getByEmail(email)
  }

  def validatePassword(email: String, enteredPwd: String): ServiceValidation[User] = {
    for {
      user <- userRepository.getByEmail(email)
      validPwd <- {
        if (passwordHasher.valid(user.password, user.salt, enteredPwd)) user.successNel[String]
        else InvalidPassword.failureNel[User]
      }
      notLocked <- UserHelper.isUserNotLocked(user)
    } yield user
  }

  def allowLogin(user: User): ServiceValidation[Boolean] = {
    user match {
      case u: ActiveUser => true.successNel[String]
      case _ =>             ServiceError("user not active").failureNel[Boolean]
    }
  }

  def register(cmd: RegisterUserCmd): Future[ServiceValidation[User]] = {
    processCommand(cmd)
  }

  def resetPassword(cmd: ResetUserPasswordCmd): Future[ServiceValidation[User]] = {
    processCommand(cmd)
  }


  def processCommand(cmd: UserCommand): Future[ServiceValidation[User]] = {
    log.debug(s"processCommand: cmd: $cmd")
    ask(processor, cmd).mapTo[ServiceValidation[UserEvent]].map { validation =>
      for {
        event <- validation
        user  <- userRepository.getByKey(UserId(event.id))
      } yield user
    }
  }
}
