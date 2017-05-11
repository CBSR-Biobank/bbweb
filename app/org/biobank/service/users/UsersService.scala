package org.biobank.service.users

import akka.actor.ActorRef
import akka.pattern.ask
import com.google.inject.ImplementedBy
import javax.inject._
import org.biobank.ValidationKey
import org.biobank.domain.access.PermissionId
import org.biobank.domain.user._
import org.biobank.domain.study.{StudyId, StudyRepository}
import org.biobank.dto._
import org.biobank.infrastructure.AscendingOrder
import org.biobank.infrastructure.command.UserCommands._
import org.biobank.infrastructure.event.UserEvents._
import org.biobank.service._
import org.biobank.service.access.AccessService
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@ImplementedBy(classOf[UsersServiceImpl])
trait UsersService extends BbwebService {

  /**
   * Returns a users.
   *
   * @param requestUserId the ID of the user making the request.
   *
   * @param id the ID of the user to return.
   */
  def getUserIfAuthorized(requestUserId: UserId, id: UserId): ServiceValidation[UserDto]

  /**
   * Should not be used by the REST API since permissions are not checked.
   *
   * @param id the ID of the user to return.
   */
  def getUser(id: UserId): ServiceValidation[User]

  /**
   * Returns a set of users. The entities can be filtered and or sorted using expressions.
   *
   * @param requestUserId the ID of the user making the reqeust
   *
   * @param filter the string representation of the filter expression to use to filter the users.
   *
   * @param sort the string representation of the sort expression to use when sorting the users.
   */
  def getUsers(requestUserId: UserId, filter: FilterString, sort: SortString): ServiceValidation[Seq[User]]

  /**
   * Returns the counts of all users and also counts of users categorized by state.
   *
   * @param requestUserId the ID of the user making the request.
   */
  def getCountsByStatus(requestUserId: UserId): ServiceValidation[UserCountsByStatus]

  def getUserStudyIds(requestUserId: UserId): ServiceValidation[Set[StudyId]]

  /**
   * Permissions not checked since anyone can attempt a login.
   */
  def loginAllowed(email: String, enteredPwd: String): ServiceValidation[User]

  /**
   * Permissions not checked since anyone can register as a user.
   */
  def register(cmd: RegisterUserCmd): Future[ServiceValidation[User]]

  /**
   * Permissions not checked since anyone can request a password reset..
   */
  def resetPassword(cmd: ResetUserPasswordCmd): Future[ServiceValidation[User]]

  def processCommand(cmd: UserCommand): Future[ServiceValidation[User]]

  def userToDto(user: User): UserDto

}

class UsersServiceImpl @javax.inject.Inject() (@Named("usersProcessor") val processor: ActorRef,
                                               val accessService:                      AccessService,
                                               val userRepository:                     UserRepository,
                                               val studyRepository:                    StudyRepository,
                                               val passwordHasher:                     PasswordHasher)
    extends UsersService
    with BbwebServiceImpl {

  import org.biobank.CommonValidations._
  import org.biobank.domain.access.AccessItem._

  case object InvalidPassword extends ValidationKey

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  def getUserIfAuthorized(requestUserId: UserId, id: UserId): ServiceValidation[UserDto] = {
    val hasPermission = accessService.hasPermission(requestUserId, PermissionId.UserRead)

    if (hasPermission.valueOr(_ => false) || (requestUserId == id)) {
      userRepository.getByKey(id).map(userToDto)
    } else {
      Unauthorized.failureNel[UserDto]
    }
  }

  def getUser(id: UserId): ServiceValidation[User] = {
    userRepository.getByKey(id)
      .leftMap(_ => IdNotFound(s"user with id does not exist: $id").nel)
  }

  def getUsers(requestUserId: UserId,
               filter:        FilterString,
               sort:          SortString): ServiceValidation[Seq[User]] = {
    val hasPermission = accessService.hasPermission(requestUserId, PermissionId.UserRead)

    if (hasPermission.valueOr(_ => false)) {
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
    } else {
      Unauthorized.failureNel[Seq[User]]
    }
  }

  def getCountsByStatus(requestUserId: UserId): ServiceValidation[UserCountsByStatus] = {
    accessService.hasPermission(requestUserId, PermissionId.UserRead).map { _ =>
      // FIXME should be replaced by DTO query to the database
      val users = userRepository.getValues
      UserCountsByStatus(
        total           = users.size.toLong,
        registeredCount = users.collect { case u: RegisteredUser => u }.size.toLong,
        activeCount     = users.collect { case u: ActiveUser     => u }.size.toLong,
        lockedCount     = users.collect { case u: LockedUser     => u }.size.toLong)
    }
  }

  def getUserStudyIds(requestUserId: UserId): ServiceValidation[Set[StudyId]] = {
    accessService.getMembership(requestUserId).map { membershipStudyIds =>
      if (membershipStudyIds.studyInfo.allStudies) studyRepository.getKeys.toSet
      else membershipStudyIds.studyInfo.studyIds
    }
  }

  def loginAllowed(email: String, enteredPwd: String): ServiceValidation[User] = {
    for {
      user <- userRepository.getByEmail(email)
      active <- user match {
        case u: ActiveUser => true.successNel[String]
        case _ =>             ServiceError("user not active").failureNel[Boolean]
      }
      validPwd <- {
        if (passwordHasher.valid(user.password, user.salt, enteredPwd)) user.successNel[String]
        else InvalidPassword.failureNel[User]
      }
    } yield user
  }

  def register(cmd: RegisterUserCmd): Future[ServiceValidation[User]] = {
    processCommand(cmd)
  }

  def resetPassword(cmd: ResetUserPasswordCmd): Future[ServiceValidation[User]] = {
    processCommand(cmd)
  }


  def processCommand(cmd: UserCommand): Future[ServiceValidation[User]] = {
    val v = cmd match {
        case c: UserStateChangeCommand =>
          accessService.hasPermission(UserId(c.sessionUserId), PermissionId.UserChangeState)
        case c: UserModifyCommand      =>
          accessService.hasPermission(UserId(c.sessionUserId), PermissionId.UserUpdate)
        case _ => true.successNel[String]
      }

    v.fold(
      err => Future.successful(Unauthorized.failureNel[User]),
      authorized => {
        if (authorized) {
          ask(processor, cmd).mapTo[ServiceValidation[UserEvent]].map { validation =>
            for {
              event <- validation
              user  <- userRepository.getByKey(UserId(event.id))
            } yield user
          }
        } else {
          Future.successful(Unauthorized.failureNel[User])
        }
      }
    )
  }

  def userToDto(user: User): UserDto = {
    UserDto(id           = user.id.id,
            version      = user.version,
            timeAdded    = user.timeAdded,
            timeModified = user.timeModified,
            state        = user.state,
            name         = user.name,
            email        = user.email,
            avatarUrl    = user.avatarUrl,
            roles        = accessService.getRoles(user.id))
  }
}
