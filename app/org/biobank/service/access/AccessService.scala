package org.biobank.service.access

import akka.actor._
import akka.pattern.ask
import com.google.inject.ImplementedBy
import javax.inject._
import org.biobank.domain.access._
import org.biobank.domain.access.PermissionId._
import org.biobank.domain.user.{UserId, UserRepository}
import org.biobank.domain.study.{StudyId, StudyRepository}
import org.biobank.domain.centre.{CentreId, CentreRepository}
import org.biobank.infrastructure.AscendingOrder
import org.biobank.infrastructure.command.AccessCommands._
import org.biobank.infrastructure.command.MembershipCommands._
import org.biobank.infrastructure.event.AccessEvents._
import org.biobank.infrastructure.event.MembershipEvents._
import org.biobank.service._
import org.slf4j.{Logger, LoggerFactory}
import play.api.Environment
import scala.concurrent.Future
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@ImplementedBy(classOf[AccessServiceImpl])
trait AccessService extends BbwebService {

  def getAccessItem(requestUserId: UserId, accessItemId: AccessItemId): ServiceValidation[AccessItem]

  def getAccessItems(requestUserId: UserId,
                     filter:        FilterString,
                     sort:          SortString): ServiceValidation[Seq[AccessItem]]

  def getRole(requestUserId: UserId, roleId: AccessItemId): ServiceValidation[Role]

  def getRoleBySlug(requestUserId: UserId, slug: String): ServiceValidation[Role]

  def getRoles(requestUserId: UserId, filter: FilterString, sort: SortString): ServiceValidation[Seq[Role]]

  def getUserRoles(userId: UserId): Set[Role]

  //def assignRole(cmd: AddUserToRoleCmd): Future[ServiceValidation[Role]]

  def hasPermission(userId: UserId, permissionId: AccessItemId): ServiceValidation[Boolean]

  def isMember(userId:   UserId,
               studyId:  Option[StudyId],
               centreId: Option[CentreId]): ServiceValidation[Boolean]

  def hasPermissionAndIsMember(userId:       UserId,
                               permissionId: AccessItemId,
                               studyId:      Option[StudyId],
                               centreId:     Option[CentreId]): ServiceValidation[Boolean]

  def getMembership(requestUserId: UserId, membershipId: MembershipId): ServiceValidation[Membership]

  def getMembershipBySlug(requestUserId: UserId, slug: String): ServiceValidation[Membership]

  def getMemberships(requestUserId: UserId,
                     filter:        FilterString,
                     sort:          SortString): ServiceValidation[Seq[Membership]]

  def getUserMembership(userId: UserId): ServiceValidation[UserMembership]

  def processRoleCommand(cmd: AccessCommand): Future[ServiceValidation[Role]]

  def processRemoveRoleCommand(cmd: RemoveRoleCmd): Future[ServiceValidation[Boolean]]

  def processMembershipCommand(cmd: MembershipCommand): Future[ServiceValidation[Membership]]

  def processRemoveMembershipCommand(cmd: MembershipCommand): Future[ServiceValidation[Boolean]]

  def snapshotRequest(requestUserId: UserId): ServiceValidation[Unit]

}

@SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
class AccessServiceImpl @Inject() (@Named("accessProcessor") val processor:         ActorRef,
                                   @Named("membershipProcessor") val membershipProcessor: ActorRef,
                                   val accessItemRepository:                              AccessItemRepository,
                                   val membershipRepository:                              MembershipRepository,
                                   val userRepository:                                    UserRepository,
                                   val studyRepository:                                   StudyRepository,
                                   val centreRepository:                                  CentreRepository,
                                   val environment:                                       Environment)
                               (implicit executionContext: BbwebExecutionContext)
    extends AccessService
    with BbwebServiceImpl {

  import org.biobank.CommonValidations._
  import org.biobank.domain.access.AccessItem._

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  def getUserRoles(userId: UserId): Set[Role] = {
    accessItemRepository.rolesForUser(userId)
  }

  def getAccessItems(requestUserId: UserId,
                     filter:        FilterString,
                     sort:          SortString): ServiceValidation[Seq[AccessItem]] = {
    whenPermitted(requestUserId, PermissionId.RoleRead) { () =>
      val allItems = accessItemRepository.getValues.toSet
      val sortStr = if (sort.expression.isEmpty) new SortString("name")
                    else sort
      for {
        items           <- AccessItemFilter.filterAccessItems(allItems, filter)
        sortExpressions <- { QuerySortParser(sortStr).
                              toSuccessNel(ServiceError(s"could not parse sort expression: $sort")) }
        firstSort       <- { sortExpressions.headOption.
                              toSuccessNel(ServiceError("at least one sort expression is required")) }
        sortFunc        <- { AccessItem.sort2Compare.get(firstSort.name).
                              toSuccessNel(ServiceError(s"invalid sort field: ${firstSort.name}")) }
      } yield {
        val result = items.toSeq.sortWith(sortFunc)
        if (firstSort.order == AscendingOrder) result
        else result.reverse
      }
    }
  }

  def getRole(requestUserId: UserId, roleId: AccessItemId): ServiceValidation[Role] = {
    whenPermitted(requestUserId, PermissionId.RoleRead) { () =>
      accessItemRepository.getRole(roleId)
    }
  }

  def getRoleBySlug(requestUserId: UserId, slug: String): ServiceValidation[Role] = {
    whenPermitted(requestUserId, PermissionId.RoleRead) { () =>
      for {
        item <- accessItemRepository.getBySlug(slug)
        role <- {
          item match {
            case role: Role => role.successNel[String]
            case _ => EntityCriteriaError(s"access item not a role: $id").failureNel[Role]
          }
        }
      } yield role
    }
  }

  def getRoles(requestUserId: UserId, filter: FilterString, sort: SortString)
    : ServiceValidation[Seq[Role]] = {
    whenPermitted(requestUserId, PermissionId.RoleRead) { () =>
      val allRoles = accessItemRepository.getRoles
      val sortStr = if (sort.expression.isEmpty) new SortString("name")
                    else sort
      for {
        roles           <- AccessItemFilter.filterRoles(allRoles, filter)
        sortExpressions <- { QuerySortParser(sortStr).
                              toSuccessNel(ServiceError(s"could not parse sort expression: $sort")) }
        firstSort       <- { sortExpressions.headOption.
                              toSuccessNel(ServiceError("at least one sort expression is required")) }
        sortFunc        <- { AccessItem.sort2Compare.get(firstSort.name).
                              toSuccessNel(ServiceError(s"invalid sort field: ${firstSort.name}")) }
      } yield {
        val result = roles.toSeq.sortWith(sortFunc)
        if (firstSort.order == AscendingOrder) result
        else result.reverse
      }
    }
  }

  // def assignRole(cmd: AddUserToRoleCmd): Future[ServiceValidation[Role]] = {
  //   for {
  //     user <- Future { userRepository.getByKey(UserId(cmd.userId)) }
  //     role <- processRoleCommand(cmd)
  //   } yield role
  // }

  def hasPermission(userId: UserId, permissionId: AccessItemId): ServiceValidation[Boolean] = {
    val v = userRepository.getByKey(userId).flatMap { _ =>
        hasPermissionInternal(userId, permissionId)
      }
    log.debug(s"hasPermission: $v")
    v
  }

  def isMember(userId:   UserId,
               studyId:  Option[StudyId],
               centreId: Option[CentreId]): ServiceValidation[Boolean] = {
    (studyId, centreId) match {
      case (None, None) => false.successNel[String]
      case _ =>
        val validBoolean = true.successNel[String]
        for {
          user         <- userRepository.getByKey(userId)
          studyValid   <- studyId.map(studyRepository.getByKey(_).map(_ => true)).getOrElse(validBoolean)
          centreValid  <- centreId.map(centreRepository.getByKey(_).map(_ => true)).getOrElse(validBoolean)
          isMember     <- isMemberInternal(userId, studyId, centreId)
        } yield isMember
    }
  }

  def hasPermissionAndIsMember(userId:       UserId,
                               permissionId: AccessItemId,
                               studyId:      Option[StudyId],
                               centreId:     Option[CentreId]): ServiceValidation[Boolean] = {
    for {
      permission <- hasPermissionInternal(userId, permissionId)
      member     <- isMemberInternal(userId, studyId, centreId)
    } yield (permission && member)
  }

  def getMembership(requestUserId: UserId, membershipId: MembershipId): ServiceValidation[Membership] = {
    whenPermitted(requestUserId, PermissionId.MembershipRead) { () =>
      membershipRepository.getByKey(membershipId)
    }
  }

  def getMembershipBySlug(requestUserId: UserId, slug: String): ServiceValidation[Membership] = {
    whenPermitted(requestUserId, PermissionId.MembershipRead) { () =>
      membershipRepository.getBySlug(slug)
    }
  }

  def getMemberships(requestUserId: UserId,
                     filter:        FilterString,
                     sort:          SortString): ServiceValidation[Seq[Membership]] = {
    whenPermitted(requestUserId, PermissionId.MembershipRead) { () =>
      val allMemberships = membershipRepository.getValues.toSet
      val sortStr = if (sort.expression.isEmpty) new SortString("name")
                    else sort
      for {
        memberships     <- MembershipFilter.filterMemberships(allMemberships, filter)
        sortExpressions <- { QuerySortParser(sortStr).
                              toSuccessNel(ServiceError(s"could not parse sort expression: $sort")) }
        firstSort       <- { sortExpressions.headOption.
                              toSuccessNel(ServiceError("at least one sort expression is required")) }
        sortFunc        <- { Membership.sort2Compare.get(firstSort.name).
                              toSuccessNel(ServiceError(s"invalid sort field: ${firstSort.name}")) }
      } yield {
        val result = memberships.toSeq.sortWith(sortFunc)

        if (firstSort.order == AscendingOrder) result
        else result.reverse
      }
    }
  }

  def getUserMembership(userId: UserId): ServiceValidation[UserMembership] = {
    membershipRepository.getUserMembership(userId)
  }

  def getAccessItem(requestUserId: UserId, accessItemId: AccessItemId): ServiceValidation[AccessItem] = {
    whenPermitted(requestUserId, PermissionId.UserRead) { () =>
      accessItemRepository.getByKey(accessItemId)
    }
  }

  def processRoleCommand(cmd: AccessCommand): Future[ServiceValidation[Role]] = {
    val v = for {
        validCommand <- {
          cmd match {
            // case c: RemoveRoleCmd =>
            //   ServiceError(s"invalid service call: $cmd, use processRemoveRoleCommand")
            //     .failureNel[Boolean]
            case c => true.successNel[String]
          }
        }
        validEntities <- {
          cmd match {
            case c: AddRoleCmd =>
              for {
                validUsers   <- c.userIds.map(id => userRepository.getByKey(UserId(id))).toList.sequenceU
                validParents <- c.parentIds.map(id => accessItemRepository.getRole(AccessItemId(id))).toList.sequenceU
                validChildren <- c.childrenIds.map(id => accessItemRepository.getByKey(AccessItemId(id))).toList.sequenceU
              } yield true

            case c: RoleAddUserCmd =>
              userRepository.getByKey(UserId(c.userId)).map(_ => true)

            case c: RoleAddParentCmd =>
              accessItemRepository.getByKey(AccessItemId(c.parentRoleId)).map(_ => true)

            case c: RoleAddChildCmd =>
              accessItemRepository.getByKey(AccessItemId(c.childRoleId)).map(_ => true)

            case c: RoleRemoveUserCmd =>
              userRepository.getByKey(UserId(c.userId)).map(_ => true)

            case c: RoleRemoveParentCmd =>
              accessItemRepository.getByKey(AccessItemId(c.parentRoleId)).map(_ => true)

            case c: RoleRemoveChildCmd =>
              accessItemRepository.getByKey(AccessItemId(c.childRoleId)).map(_ => true)

            case _ =>
              true.successNel[String]
          }
        }
        permitted <- {
          val permissionId = cmd match {
              case c: AddRoleCmd        => PermissionId.RoleCreate
              case c: RoleModifyCommand => PermissionId.RoleUpdate
            }
          hasPermissionInternal(UserId(cmd.sessionUserId), permissionId)
        }
      } yield permitted

    log.debug(s"processRoleCommand: cmd: $cmd")
    v.fold(
      err => Future.successful(err.failure[Role]),
      permitted => {
        if (!permitted) {
          Future.successful(Unauthorized.failureNel[Role])
        } else {
          ask(processor, cmd).mapTo[ServiceValidation[AccessEvent]].map { validation =>
            validation.flatMap { event =>
              if (event.eventType.isRole) {
                accessItemRepository.getRole(AccessItemId(event.getRole.getId))
              } else {
                ServiceError("Server Error: event is not for role").failureNel[Role]
              }
            }
          }
        }
      }
    )
  }

  def processRemoveRoleCommand(cmd: RemoveRoleCmd): Future[ServiceValidation[Boolean]] = {
    hasPermissionInternal(UserId(cmd.sessionUserId), PermissionId.RoleDelete).fold(
      err => Future.successful(err.failure[Boolean]),
      permitted => {
        if (!permitted) {
          Future.successful(Unauthorized.failureNel[Boolean])
        } else {
          ask(processor, cmd).mapTo[ServiceValidation[AccessEvent]].map { validation =>
            validation.map(_ => true)
          }
        }
      }
    )
  }

  // should only called if userId is valid
  private def hasPermissionInternal(userId: UserId, permissionId: AccessItemId)
      : ServiceValidation[Boolean] = {

    def hasPermissionParents(parentIds: Set[AccessItemId]): Boolean = {
      val found = parentIds.find { id =>
          accessItemRepository.getByKey(id).fold(
            err => false,
            item => checkItemAccess(item)
          )
        }.toSuccessNel("not allowed")
      log.debug(s"hasPermissionParents: ${found}")
      found.fold(err => false, _ => true)
    }

    def checkItemAccess(item: AccessItem): Boolean = {
      item match {
        case permission: Permission =>
          log.debug(s"checkItemAccess: ${item.id}, checking permission parents: ${permission.parentIds}")
          hasPermissionParents(permission.parentIds)

        case role: Role =>
          val result = if (role.userIds.exists(_ == userId)) {
              true
            } else {
              log.debug(s"checkItemAccess: ${item.id}, checking role parents")
              hasPermissionParents(role.parentIds)
            }
          log.debug(s"checkItemAccess: role: ${item.id}, result: $result")
          result
      }
    }

    log.debug(s"hasPermission: userId: $userId, permissionId: $permissionId")
    accessItemRepository.getByKey(permissionId).map(checkItemAccess)
  }

  def processMembershipCommand(cmd: MembershipCommand): Future[ServiceValidation[Membership]] = {
    val v = for {
        validCommand <- {
          cmd match {
            case c: RemoveMembershipCmd =>
              ServiceError(s"invalid service call: $cmd, use processRemoveMembershipCommand")
                .failureNel[Boolean]
            case c => true.successNel[String]
          }
        }
        validEntities <- {
          cmd match {
            case c: AddMembershipCmd => {
              for {
                usersNonEmpty <- {
                  if (c.userIds.isEmpty) ServiceError("userIds cannot be empty").failureNel[Boolean]
                  else true.successNel[String]
                }
                validUsers   <- c.userIds.map(id => userRepository.getByKey(UserId(id))).toList.sequenceU
                validStudies <- c.studyIds.map(id => studyRepository.getByKey(StudyId(id))).toList.sequenceU
                validCentres <- c.centreIds.map(id => centreRepository.getByKey(CentreId(id))).toList.sequenceU
              } yield true
            }

            case c: MembershipAddUserCmd =>
              userRepository.getByKey(UserId(c.userId)).map(_ => true)

            case c: MembershipAddStudyCmd =>
              studyRepository.getByKey(StudyId(c.studyId)).map(_ => true)

            case c: MembershipAddCentreCmd =>
              centreRepository.getByKey(CentreId(c.centreId)).map(_ => true)

            case c: MembershipRemoveUserCmd =>
              userRepository.getByKey(UserId(c.userId)).map(_ => true)

            case c: MembershipRemoveStudyCmd =>
              studyRepository.getByKey(StudyId(c.studyId)).map(_ => true)

            case c: MembershipRemoveCentreCmd =>
              centreRepository.getByKey(CentreId(c.centreId)).map(_ => true)

            case _ =>
              true.successNel[String]
          }
        }
        permitted <- {
          val permissionId = cmd match {
              case c: AddMembershipCmd        => PermissionId.MembershipCreate
              case c: MembershipModifyCommand => PermissionId.MembershipUpdate
            }
          hasPermissionInternal(UserId(cmd.sessionUserId), permissionId)
        }
      } yield permitted

    v.fold(
      err => Future.successful(err.failure[Membership]),
      permitted => {
        if (!permitted) {
          Future.successful(Unauthorized.failureNel[Membership])
        } else {
          ask(membershipProcessor, cmd).mapTo[ServiceValidation[MembershipEvent]].map { validation =>
            validation.flatMap { event =>
              membershipRepository.getByKey(MembershipId(event.id))
            }
          }
        }
      }
    )
  }

  def processRemoveMembershipCommand(cmd: MembershipCommand): Future[ServiceValidation[Boolean]] = {
    hasPermissionInternal(UserId(cmd.sessionUserId), PermissionId.MembershipDelete).fold(
      err => Future.successful(err.failure[Boolean]),
      permitted => {
        if (!permitted) {
          Future.successful(Unauthorized.failureNel[Boolean])
        } else {
          ask(membershipProcessor, cmd).mapTo[ServiceValidation[MembershipEvent]].map { validation =>
            validation.map(_ => true)
          }
        }
      }
    )
  }

  // should only called if userId is valid, studyId is None or valid, and centreId is None or valid
  private def isMemberInternal(userId:   UserId,
                               studyId:  Option[StudyId],
                               centreId: Option[CentreId]): ServiceValidation[Boolean] = {

    val membership = membershipRepository
      .getUserMembership(userId).map { membership =>
        membership.isMember(studyId, centreId)
      }

    log.debug(s"isMemberInternal: userId: $userId, studyId: $studyId, centreId: $centreId, membership: $membership")
    membership
  }

  private def whenPermitted[T](requestUserId: UserId, permissionId: PermissionId)
                           (block: () => ServiceValidation[T]): ServiceValidation[T] = {
    hasPermission(requestUserId, permissionId).fold(
      err        => err.failure[T],
      permission => if (permission) block()
                    else Unauthorized.failureNel[T]
    )
  }
}
