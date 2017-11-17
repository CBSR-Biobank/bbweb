package org.biobank.service.users

import akka.actor.ActorRef
import akka.pattern.ask
import com.google.inject.ImplementedBy
import javax.inject._
import org.biobank.ValidationKey
import org.biobank.domain.access.PermissionId
import org.biobank.domain.centre.CentreRepository
import org.biobank.domain.study.{StudyId, StudyRepository}
import org.biobank.domain.user._
import org.biobank.infrastructure.AscendingOrder
import org.biobank.infrastructure.command.UserCommands._
import org.biobank.infrastructure.event.UserEvents._
import org.biobank.service._
import org.biobank.service.access.AccessService
import org.slf4j.{Logger, LoggerFactory}
import scala.concurrent.Future
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@ImplementedBy(classOf[UsersServiceImpl])
trait UsersService extends BbwebService {

  /**
   * Returns a user.
   *
   * @param requestUserId the ID of the user making the request.
   *
   * @param id the ID of the user to return.
   */
  def getUserIfAuthorized(requestUserId: UserId, id: UserId): ServiceValidation[User]

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

  def snapshotRequest(requestUserId: UserId): ServiceValidation[Unit]

}

@SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
class UsersServiceImpl @javax.inject.Inject()(@Named("usersProcessor") val processor: ActorRef,
                                              val accessService:                      AccessService,
                                              val userRepository:                     UserRepository,
                                              val studyRepository:                    StudyRepository,
                                              val centreRepository:                   CentreRepository,
                                              val passwordHasher:                     PasswordHasher)
                                           (implicit executionContext: BbwebExecutionContext)
    extends UsersService
    with AccessChecksSerivce
    with ServicePermissionChecks {

  import org.biobank.CommonValidations._
  import org.biobank.domain.access.AccessItem._

  case object InvalidPassword extends ValidationKey

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  def getUserIfAuthorized(requestUserId: UserId, id: UserId): ServiceValidation[User] = {
    accessService.hasPermission(requestUserId, PermissionId.UserRead).flatMap { permission =>
      if (permission || (requestUserId == id)) {
        userRepository.getByKey(id)
      } else {
        Unauthorized.failureNel[User]
      }
    }
  }

  def getUser(id: UserId): ServiceValidation[User] = {
    userRepository.getByKey(id)
      .leftMap(_ => IdNotFound(s"user with id does not exist: $id").nel)
  }

  def getUsers(requestUserId: UserId,
               filter:        FilterString,
               sort:          SortString): ServiceValidation[Seq[User]] = {
    whenPermitted(requestUserId, PermissionId.UserRead) { () =>
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
  }

  def getCountsByStatus(requestUserId: UserId): ServiceValidation[UserCountsByStatus] = {
    whenPermitted(requestUserId, PermissionId.UserRead) { () =>
      val users = userRepository.getValues
      UserCountsByStatus(
        total           = users.size.toLong,
        registeredCount = users.collect { case u: RegisteredUser => u }.size.toLong,
        activeCount     = users.collect { case u: ActiveUser     => u }.size.toLong,
        lockedCount     = users.collect { case u: LockedUser     => u }.size.toLong
      ).successNel[String]
    }
  }

  def getUserStudyIds(requestUserId: UserId): ServiceValidation[Set[StudyId]] = {
    accessService.getUserMembership(requestUserId).map { membershipStudyIds =>
      if (membershipStudyIds.studyData.allEntities) studyRepository.getKeys.toSet
      else membershipStudyIds.studyData.ids
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
        case c: UserModifyCommand =>
          accessService.hasPermission(UserId(c.sessionUserId), PermissionId.UserUpdate)
            .map { permission =>
              // user is allowed to change his / her own settings
              permission || (c.sessionUserId == c.id)
            }
        case _ =>
          // anonymous user can register as users
          true.successNel[String]
      }

    v.fold(
      err => Future.successful(err.failure[User]),
      permitted => if (permitted) {
        ask(processor, cmd).mapTo[ServiceValidation[UserEvent]].map { validation =>
          for {
            event <- validation
            user  <- userRepository.getByKey(UserId(event.id))
          } yield user
        }
      } else {
        Future.successful(Unauthorized.failureNel[User])
      }
    )
  }

}
