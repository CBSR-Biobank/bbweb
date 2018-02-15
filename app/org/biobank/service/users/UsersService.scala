package org.biobank.service.users

import akka.actor.ActorRef
import akka.pattern.ask
import com.google.inject.ImplementedBy
import java.time.format.DateTimeFormatter
import javax.inject._
import org.biobank.ValidationKey
import org.biobank.domain.access.AccessItemId
import org.biobank.domain.access.PermissionId
import org.biobank.domain.centre.CentreRepository
import org.biobank.domain.study.{Study, StudyId, StudyRepository}
import org.biobank.domain.user._
import org.biobank.dto._
import org.biobank.dto.access.{RoleDto, UserRoleDto}
import org.biobank.infrastructure.AscendingOrder
import org.biobank.infrastructure.command.AccessCommands._
import org.biobank.infrastructure.command.UserCommands._
import org.biobank.infrastructure.event.UserEvents._
import org.biobank.service._
import org.biobank.service.access.AccessService
import org.biobank.service.studies.StudiesService
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
  def getUserIfAuthorized(requestUserId: UserId, id: UserId): ServiceValidation[UserDto]

  /**
   * Returns a user for the given slug.
   *
   * @param requestUserId the ID of the user making the request.
   *
   * @param slug the slug for the given user.
   */
  def getUserBySlug(requestUserId: UserId, slug: String): ServiceValidation[UserDto]

  /**
   * Should not be used by the REST API since permissions are not checked.
   *
   * @param id the ID of the user to return.
   */
  def getUser(id: UserId): ServiceValidation[User]

  /**
   * Returns a set of users. The entities can be filtered and or sorted using expressions.
   *
   * @param requestUserId the ID of the user making the request
   *
   * @param filter the string representation of the filter expression to use to filter the users.
   *
   * @param sort the string representation of the sort expression to use when sorting the users.
   */
  def getUsers(requestUserId: UserId, filter: FilterString, sort: SortString)
      : ServiceValidation[Seq[UserDto]]

  /**
   * Returns the counts of all users and also counts of users categorized by state.
   *
   * @param requestUserId the ID of the user making the request.
   */
  def getCountsByStatus(requestUserId: UserId): ServiceValidation[UserCountsByStatus]

  /**
   * Returns a set of studies that the user has access to. The studies can be filtered and sorted using an
   * expression.
   *
   * @param userId the ID of the user to retrieve the list of studies for.
   *
   * @param filter the string representation of the filter expression to use to filter the studies.
   *
   * @param sort the string representation of the sort expression to use when sorting the studies.
   */
  def getUserStudies(userId: UserId, filter: FilterString, sort: SortString)
      : ServiceValidation[Seq[Study]]

  def getUserStudyIds(requestUserId: UserId): ServiceValidation[Set[StudyId]]

  /**
   * Permissions not checked since anyone can attempt a login.
   */
  def loginAllowed(email: String, enteredPwd: String): ServiceValidation[UserDto]

  /**
   * Permissions not checked since anyone can register as a user.
   */
  def register(cmd: RegisterUserCmd): Future[ServiceValidation[UserDto]]

  /**
   * Permissions not checked since anyone can request a password reset..
   */
  def resetPassword(cmd: ResetUserPasswordCmd): Future[ServiceValidation[UserDto]]

  def processCommand(cmd: UserCommand): Future[ServiceValidation[UserDto]]

  def snapshotRequest(requestUserId: UserId): ServiceValidation[Unit]

}

@SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
class UsersServiceImpl @javax.inject.Inject()(@Named("usersProcessor") val processor: ActorRef,
                                              val accessService:                      AccessService,
                                              val studiesService:                     StudiesService,
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

  def getUserIfAuthorized(requestUserId: UserId, id: UserId): ServiceValidation[UserDto] = {
    accessService.hasPermission(requestUserId, PermissionId.UserRead).flatMap { permission =>
      if (permission || (requestUserId == id)) {
        userRepository.getByKey(id) flatMap { user =>
          userToDto(user, accessService.getUserRoles(user.id))
        }
      } else {
        Unauthorized.failureNel[UserDto]
      }
    }
  }

  def getUserBySlug(requestUserId: UserId, slug: String): ServiceValidation[UserDto] = {
    accessService.hasPermission(requestUserId, PermissionId.UserRead).flatMap { permission =>
      userRepository.getBySlug(slug).flatMap { user =>
        if (permission || (requestUserId == user.id)) {
          userToDto(user, accessService.getUserRoles(user.id))
        } else {
          Unauthorized.failureNel[UserDto]
        }
      }
    }
  }

  def getUser(id: UserId): ServiceValidation[User] = {
    userRepository.getByKey(id)
      .leftMap(_ => IdNotFound(s"user with id does not exist: $id").nel)
  }

  def getUsers(requestUserId: UserId,
               filter:        FilterString,
               sort:          SortString): ServiceValidation[Seq[UserDto]] = {
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
        dtos            <- {
          users
            .toSeq
            .sortWith(sortFunc)
            .map(user => userToDto(user, accessService.getUserRoles(user.id)))
            .toList.sequenceU
            .map(_.toSeq)
        }
      } yield {
        if (firstSort.order == AscendingOrder) dtos
        else dtos.reverse
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

  def getUserStudies(userId: UserId, filter: FilterString, sort: SortString)
      : ServiceValidation[Seq[Study]] = {
    studiesService.getStudies(userId, filter, sort)
  }

  def getUserStudyIds(requestUserId: UserId): ServiceValidation[Set[StudyId]] = {
    accessService.getUserMembership(requestUserId).map { membershipStudyIds =>
      if (membershipStudyIds.studyData.allEntities) studyRepository.getKeys.toSet
      else membershipStudyIds.studyData.ids
    }
  }

  def loginAllowed(email: String, enteredPwd: String): ServiceValidation[UserDto] = {
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
      dto <- userToDto(user, accessService.getUserRoles(user.id))
    } yield dto
  }

  def register(cmd: RegisterUserCmd): Future[ServiceValidation[UserDto]] = {
    processCommand(cmd)
  }

  def resetPassword(cmd: ResetUserPasswordCmd): Future[ServiceValidation[UserDto]] = {
    processCommand(cmd)
  }


  def processCommand(cmd: UserCommand): Future[ServiceValidation[UserDto]] = {

    def processIfPermitted(validation: ServiceValidation[Boolean]) = {
      validation.fold(
        err => Future.successful(err.failure[UserDto]),
        permitted => if (permitted) {
          ask(processor, cmd).mapTo[ServiceValidation[UserEvent]].map { validation =>
            for {
              event <- validation
              user  <- userRepository.getByKey(UserId(event.id))
              dto   <- userToDto(user, accessService.getUserRoles(user.id))
            } yield dto
          }
        } else {
          Future.successful(Unauthorized.failureNel[UserDto])
        }
      )
    }

    // asks the access service to process a command
    def accessServiceProcess(userId: UserId, validation: ServiceValidation[AccessModifyCommand])
    : Future[ServiceValidation[UserDto]] = {
      validation.fold(
        err => Future.successful(err.failure[UserDto]),
        command => {
          accessService.processRoleCommand(command).mapTo[ServiceValidation[RoleDto]].map { v =>
            for {
              event <- v
              user  <- userRepository.getByKey(userId)
              dto   <- userToDto(user, accessService.getUserRoles(userId))
            } yield dto
          }
        }
      )
    }

    cmd match {
      case c: UpdateUserAddRoleCmd =>
        val v = accessService.getRole(UserId(c.sessionUserId), AccessItemId(c.roleId)).map { role =>
            RoleAddUserCmd(sessionUserId   = c.sessionUserId,
                           expectedVersion = role.version,
                           roleId          = c.roleId,
                           userId          = c.id)
        }
        accessServiceProcess(UserId(c.id), v)

      case c: UpdateUserRemoveRoleCmd =>
        val v = accessService.getRole(UserId(c.sessionUserId), AccessItemId(c.roleId)).map { role =>
            RoleRemoveUserCmd(sessionUserId   = c.sessionUserId,
                              expectedVersion = role.version,
                              roleId          = c.roleId,
                              userId          = c.id)
        }
        accessServiceProcess(UserId(c.id), v)

      case c: UserStateChangeCommand =>
        val v = accessService.hasPermission(UserId(c.sessionUserId), PermissionId.UserChangeState)
        processIfPermitted(v)

      case c: UserModifyCommand =>
        val v = accessService.hasPermission(UserId(c.sessionUserId), PermissionId.UserUpdate)
          .map { permission =>
            // user is allowed to change his / her own settings
            permission || (c.sessionUserId == c.id)
          }
        processIfPermitted(v)

      case _ =>
        // anonymous user can register as users
        processIfPermitted(true.successNel[String])
    }
  }

  private def userToDto(user: User, roles: ServiceValidation[Set[UserRoleDto]])
      : ServiceValidation[UserDto] = {
    roles.map { roles =>
      UserDto(id           = user.id.id,
              version      = user.version,
              timeAdded    = user.timeAdded.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
              timeModified = user.timeModified.map(_.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)),
              state        = user.state,
              slug         = user.slug,
              name         = user.name,
              email        = user.email,
              avatarUrl    = user.avatarUrl,
              roles       = roles,
              membership   = None)
    }
  }

}
