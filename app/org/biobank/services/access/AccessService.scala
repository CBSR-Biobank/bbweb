package org.biobank.services.access

import akka.actor._
import akka.pattern.ask
import com.google.inject.ImplementedBy
import java.time.format.DateTimeFormatter
import javax.inject._
import org.biobank.domain.{ConcurrencySafeEntity, HasName, HasSlug, IdentifiedValueObject, Slug}
import org.biobank.domain.access._
import org.biobank.domain.access.PermissionId._
import org.biobank.domain.users.{UserId, UserRepository}
import org.biobank.domain.studies.{StudyId, StudyRepository}
import org.biobank.domain.centres.{CentreId, CentreRepository}
import org.biobank.dto._
import org.biobank.dto.access._
import org.biobank.infrastructure.AscendingOrder
import org.biobank.infrastructure.commands.AccessCommands._
import org.biobank.infrastructure.commands.MembershipCommands._
import org.biobank.infrastructure.events.AccessEvents._
import org.biobank.infrastructure.events.MembershipEvents._
import org.biobank.services._
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
                     sort:          SortString)
      : Future[ServiceValidation[Seq[AccessItemNameDto]]]

  def getRole(requestUserId: UserId, roleId: AccessItemId): ServiceValidation[RoleDto]

  def getRoleBySlug(requestUserId: UserId, slug: Slug): ServiceValidation[RoleDto]

  def getRoles(requestUserId: UserId, pagedQuery: PagedQuery)
      : Future[ServiceValidation[PagedResults[RoleDto]]]

  def getRoleNames(requestUserId: UserId, query: FilterAndSortQuery)
      : Future[ServiceValidation[Seq[EntityInfoDto]]]

  def getUserRoles(userId: UserId): ServiceValidation[Set[UserRoleDto]]

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

  def getMembershipBySlug(requestUserId: UserId, slug: Slug): ServiceValidation[MembershipDto]

  def getMemberships(requestUserId: UserId, pagedQuery: PagedQuery)
      : Future[ServiceValidation[PagedResults[MembershipDto]]]

  def getMembershipNames(requestUserId: UserId, query: FilterAndSortQuery)
      : Future[ServiceValidation[Seq[EntityInfoDto]]]

  def getUserMembership(userId: UserId): ServiceValidation[UserMembership]

  def getUserMembershipDto(userId: UserId): ServiceValidation[UserMembershipDto]

  def processRoleCommand(cmd: AccessCommand): Future[ServiceValidation[RoleDto]]

  def processRemoveRoleCommand(cmd: RemoveRoleCmd): Future[ServiceValidation[Boolean]]

  def processMembershipCommand(cmd: MembershipCommand): Future[ServiceValidation[MembershipDto]]

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

  def getUserRoles(userId: UserId): ServiceValidation[Set[UserRoleDto]] = {
    accessItemRepository.rolesForUser(userId).map(roleToUserRoleDto).toList.sequenceU.map(_.toSet)
  }

  def getAccessItems(requestUserId: UserId,
                     filter:        FilterString,
                     sort:          SortString): Future[ServiceValidation[Seq[AccessItemNameDto]]] = {
    Future {
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
          val sortedItems = items.toSeq.sortWith(sortFunc)
          val dtos =         sortedItems.map(i => AccessItemNameDto(i.id.id,
                                                                    i.slug,
                                                                    i.name,
                                                                    i.accessItemType.id))
          if (firstSort.order == AscendingOrder) dtos
          else dtos.reverse
        }
      }
    }
  }

  def getRole(requestUserId: UserId, roleId: AccessItemId): ServiceValidation[RoleDto] = {
    whenPermitted(requestUserId, PermissionId.RoleRead) { () =>
      for {
        role <- accessItemRepository.getRole(roleId)
        dto <- roleToDto(role)
      } yield dto
    }
  }

  def getRoleBySlug(requestUserId: UserId, slug: Slug): ServiceValidation[RoleDto] = {
    whenPermitted(requestUserId, PermissionId.RoleRead) { () =>
      for {
        item <- accessItemRepository.getBySlug(slug)
        role <- {
          item match {
            case role: Role => role.successNel[String]
            case _ => EntityCriteriaError(s"access item not a role: $id").failureNel[Role]
          }
        }
        dto <- roleToDto(role)
      } yield dto
    }
  }

   def getRoles(requestUserId: UserId, query: PagedQuery)
       : Future[ServiceValidation[PagedResults[RoleDto]]] = {
     Future {
       whenPermitted(requestUserId, PermissionId.RoleRead) { () =>
         for {
           sortedRoles <- getRolesInternal(query.filter, query.sort)
           validPage   <- query.validPage(sortedRoles.size)
           dtos        <- sortedRoles.map(roleToDto).toList.sequenceU
           result      <- PagedResults.create(dtos, query.page, query.limit)
         } yield result
       }
     }
  }

  def getRoleNames(requestUserId: UserId, query: FilterAndSortQuery)
      : Future[ServiceValidation[Seq[EntityInfoDto]]] = {
    Future {
      getRolesInternal(query.filter, query.sort).map {
        _.map { r => EntityInfoDto(r.id.id, r.slug, r.name) }
      }
    }
  }

  private def getRolesInternal(filter: FilterString, sort: SortString)
      : ServiceValidation[Seq[Role]] = {
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
        val sorted = roles.toSeq.sortWith(sortFunc)
        if (firstSort.order == AscendingOrder) sorted
        else sorted.reverse
      }
  }

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
    } yield {
      //log.info(s"hasPermissionAndIsMember: permission: $permissionId, member: $member")

      (permission && member)
    }
  }

  def getMembership(requestUserId: UserId, membershipId: MembershipId): ServiceValidation[Membership] = {
    whenPermitted(requestUserId, PermissionId.MembershipRead) { () =>
      membershipRepository.getByKey(membershipId)
    }
  }

  def getMembershipBySlug(requestUserId: UserId, slug: Slug): ServiceValidation[MembershipDto] = {
    whenPermitted(requestUserId, PermissionId.MembershipRead) { () =>
      membershipRepository.getBySlug(slug).flatMap(membershipToDto)
    }
  }

   def getMemberships(requestUserId: UserId, query: PagedQuery)
       : Future[ServiceValidation[PagedResults[MembershipDto]]] = {
     Future {
       whenPermitted(requestUserId, PermissionId.MembershipRead) { () =>
         for {
           memberships <- getMembershipsInternal(query.filter, query.sort)
           validPage <- query.validPage(memberships.size)
           dtos        <- memberships.map(membershipToDto).toList.sequenceU
           result      <- PagedResults.create(dtos, query.page, query.limit)
         } yield result
       }
     }
   }

  def getMembershipNames(requestUserId: UserId, query: FilterAndSortQuery)
      : Future[ServiceValidation[Seq[EntityInfoDto]]] = {
    Future {
      getMembershipsInternal(query.filter, query.sort).map {
        _.map { m => EntityInfoDto(m.id.id, m.slug, m.name) }
      }
    }
  }

  private def getMembershipsInternal(filter: FilterString, sort: SortString)
      : ServiceValidation[Seq[Membership]] = {
    val allMemberships = membershipRepository.getValues.toSet
    val sortStr = if (sort.expression.isEmpty) new SortString("name")
                  else sort
    for {
      memberships     <- MembershipFilter.filterMemberships(allMemberships, filter)
      sortExpressions <- {
        QuerySortParser(sortStr).
          toSuccessNel(ServiceError(s"could not parse sort expression: $sort"))
      }
      firstSort       <- {
        sortExpressions.headOption.
          toSuccessNel(ServiceError("at least one sort expression is required"))
      }
      sortFunc        <- {
        Membership.sort2Compare.get(firstSort.name).
          toSuccessNel(ServiceError(s"invalid sort field: ${firstSort.name}"))
      }
    } yield {
      val sorted = memberships.toSeq.sortWith(sortFunc)
      if (firstSort.order == AscendingOrder) sorted
      else sorted.reverse
    }
  }

  def getUserMembership(userId: UserId): ServiceValidation[UserMembership] = {
    membershipRepository.getUserMembership(userId)
  }

  def getUserMembershipDto(userId: UserId): ServiceValidation[UserMembershipDto] = {
    getUserMembership(userId).flatMap(userMembershipToDto)
  }

  def getAccessItem(requestUserId: UserId, accessItemId: AccessItemId): ServiceValidation[AccessItem] = {
    whenPermitted(requestUserId, PermissionId.UserRead) { () =>
      accessItemRepository.getByKey(accessItemId)
    }
  }

  def processRoleCommand(cmd: AccessCommand): Future[ServiceValidation[RoleDto]] = {
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
      err => Future.successful(err.failure[RoleDto]),
      permitted => {
        if (!permitted) {
          Future.successful(Unauthorized.failureNel[RoleDto])
        } else {
          ask(processor, cmd).mapTo[ServiceValidation[AccessEvent]].map { validation =>
            validation.flatMap { event =>
              if (event.eventType.isRole) {
                accessItemRepository.getRole(AccessItemId(event.getRole.getId)).flatMap(roleToDto)
              } else {
                ServiceError("Server Error: event is not for role").failureNel[RoleDto]
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

  def processMembershipCommand(cmd: MembershipCommand): Future[ServiceValidation[MembershipDto]] = {
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
      err => Future.successful(err.failure[MembershipDto]),
      permitted => {
        if (!permitted) {
          Future.successful(Unauthorized.failureNel[MembershipDto])
        } else {
          ask(membershipProcessor, cmd).mapTo[ServiceValidation[MembershipEvent]].map { validation =>
            validation.flatMap { event =>
              membershipRepository.getByKey(MembershipId(event.id)).flatMap(membershipToDto)
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

  private def roleToDto(role: Role): ServiceValidation[RoleDto] = {

    def getAccessItems(ids: Set[AccessItemId]): ServiceValidation[Set[AccessItem]] = {
      ids
        .map { id => accessItemRepository.getByKey(id) }
        .toList.sequenceU
        .leftMap(err => org.biobank.CommonValidations.InternalServerError.nel)
        .map(_.toSet)
    }

    for {
      users    <- role.userIds.map(userRepository.getByKey).toList.sequenceU.map(_.toSet)
      parents  <- getAccessItems(role.parentIds)
      children <- getAccessItems(role.childrenIds)
    } yield {
      RoleDto(id             = role.id.id,
              version        = role.version,
              timeAdded      = role.timeAdded.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
              timeModified   = role.timeModified.map(_.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)),
              accessItemType = role.accessItemType.id,
              slug           = role.slug,
              name           = role.name,
              description    = role.description,
              userData       = entityInfoDto(users),
              parentData     = entityInfoDto(parents),
              childData      = entityInfoDto(children))
    }
  }

  private def roleToUserRoleDto(role: Role): ServiceValidation[UserRoleDto] = {

    @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
    def getAccessItems(ids: Set[AccessItemId]): ServiceValidation[List[AccessItem]] = {
      ids
        .toList
        .flatMap { id =>
          val items = for {
              item       <- accessItemRepository.getByKey(id)
              childItems <- getAccessItems(item.childrenIds)
            } yield List(item) ++ childItems
          items.sequenceU
        }.sequenceU
    }

    getAccessItems(role.childrenIds) map { children =>
      UserRoleDto(id             = role.id.id,
                  version        = role.version,
                  slug           = role.slug,
                  name           = role.name,
                  childData      = entityInfoDto(children.toSet))
    }
  }

  private def membershipToDto(membership: Membership): ServiceValidation[MembershipDto] ={
    for {
      users           <- membership.userIds.map(userRepository.getByKey).toList.sequenceU.map(_.toSet)
      studyEntitySet  <- membershipStudyDataToEntitySet(membership)
      centreEntitySet <- membershipCentreDataToEntitySet(membership)
    } yield {
      val userData        = entityInfoDto(users)
      val timeAdded       = membership.timeAdded.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
      val timeModified    = membership.timeModified.map(_.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))

      MembershipDto(id           = membership.id.id,
                    version      = membership.version,
                    timeAdded    = timeAdded,
                    timeModified = timeModified,
                    slug         = membership.slug,
                    name         = membership.name,
                    description  = membership.description,
                    userData     = userData,
                    studyData    = studyEntitySet,
                    centreData   = centreEntitySet)
    }
  }

  private def userMembershipToDto(membership: UserMembership): ServiceValidation[UserMembershipDto] ={
    for {
      studyEntitySet  <- membershipStudyDataToEntitySet(membership)
      centreEntitySet <- membershipCentreDataToEntitySet(membership)
    } yield {
      val timeAdded       = membership.timeAdded.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
      val timeModified    = membership.timeModified.map(_.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))

      UserMembershipDto(id           = membership.id.id,
                        version      = membership.version,
                        timeAdded    = timeAdded,
                        timeModified = timeModified,
                        slug         = membership.slug,
                        name         = membership.name,
                        description  = membership.description,
                        studyData    = studyEntitySet,
                        centreData   = centreEntitySet)
    }
  }

  private def membershipStudyDataToEntitySet(membership: MembershipBase)
      : ServiceValidation[EntitySetDto] = {
    idsToEntities(membership.studyData.ids, studyRepository.getByKey).map { studies =>
      entitySetDto(membership.studyData.allEntities, studies)
    }
  }

  private def membershipCentreDataToEntitySet(membership: MembershipBase)
      : ServiceValidation[EntitySetDto] = {
    idsToEntities(membership.centreData.ids, centreRepository.getByKey).map { centres =>
      entitySetDto(membership.centreData.allEntities, centres)
    }
  }

  private def idsToEntities[I <: IdentifiedValueObject[_], E <: ConcurrencySafeEntity[I]]
    (ids: Set[I], getEntity: I => ServiceValidation[E]): ServiceValidation[Set[E]] = {
    ids
      .map(id => getEntity(id))
      .toList
      .sequenceU
      .map(_.toSet)
  }

  private def entityInfoDto[T <: ConcurrencySafeEntity[_] with HasName with HasSlug]
    (entities: Set[T]): Set[EntityInfoDto] = {
    entities.map { entity => EntityInfoDto(entity.id.toString, entity.slug, entity.name) }
  }

  private def entitySetDto[T <: ConcurrencySafeEntity[_] with HasName with HasSlug]
    (hasAllEntities: Boolean, entities: Set[T]): EntitySetDto = {
    EntitySetDto(hasAllEntities, entityInfoDto(entities))
  }
}
