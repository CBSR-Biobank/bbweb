package org.biobank.controllers.access

import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}
import org.biobank.controllers._
import org.biobank.domain.{ConcurrencySafeEntity, HasUniqueName}
import org.biobank.domain.access._
import org.biobank.domain.centre.CentreId
import org.biobank.domain.study.StudyId
import org.biobank.domain.user.{User, UserId}
import org.biobank.dto._
import org.biobank.dto.access._
import org.biobank.service.PagedResults
import org.biobank.service.access.AccessService
import org.biobank.service.centres.CentresService
import org.biobank.service.studies.StudiesService
import org.biobank.service.users.UsersService
import play.api.Logger
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc._
import play.api.{Environment, Logger}
import scala.concurrent.{ExecutionContext, Future}
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._
import scalaz._

@SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
@Singleton
class AccessController @Inject() (controllerComponents: ControllerComponents,
                                  val action:           BbwebAction,
                                  val env:              Environment,
                                  val accessService:    AccessService,
                                  val studiesService:   StudiesService,
                                  val centresService:   CentresService,
                                  val usersService:     UsersService)
                              (implicit val ec: ExecutionContext)
    extends CommandController(controllerComponents) {

  import org.biobank.infrastructure.command.AccessCommands._
  import org.biobank.infrastructure.command.MembershipCommands._

  val log: Logger = Logger(this.getClass)

  private val PageSizeMax = 20

  def listItemNames: Action[Unit] =
    action.async(parse.empty) { implicit request =>
      validationReply(
        Future {
          for {
            filterAndSort <- FilterAndSortQuery.create(request.rawQueryString)
            items         <- accessService.getAccessItems(request.authInfo.userId,
                                                          filterAndSort.filter,
                                                          filterAndSort.sort)
          } yield {
            items.map(i => AccessItemNameDto(i.id.id, i.name, i.accessItemType.id))
          }
        }
      )
    }

  def listRoles: Action[Unit] =
    action.async(parse.empty) { implicit request =>
      validationReply(
        Future {
          for {
            pagedQuery <- PagedQuery.create(request.rawQueryString, PageSizeMax)
            roles      <- accessService.getRoles(request.authInfo.userId,
                                                 pagedQuery.filter,
                                                 pagedQuery.sort)
            validPage  <- pagedQuery.validPage(roles.size)
            dtos       <- {
              roles.map(role => roleToDto(request.authInfo.userId, role))
                .toList.sequenceU.map(_.toSeq)
            }
            results    <- PagedResults.create(dtos, pagedQuery.page, pagedQuery.limit)
          } yield results
        }
      )
    }

  def listRoleNames: Action[Unit] =
    action.async(parse.empty) { implicit request =>
      validationReply(
        Future {
          for {
            filterAndSort <- FilterAndSortQuery.create(request.rawQueryString)
            roles         <- accessService.getRoles(request.authInfo.userId,
                                                    filterAndSort.filter,
                                                    filterAndSort.sort)
          } yield {
            roles.map(r => NameDto(r.id.id, r.name))
          }
        }
      )
    }

  def getRole(roleId: AccessItemId): Action[Unit] =
    action(parse.empty) { implicit request =>
      val v = accessService.getRole(request.authInfo.userId, roleId)
        .flatMap(role => roleToDto(request.authInfo.userId, role))
      validationReply(v)
    }

  def getMembership(membershipId: MembershipId): Action[Unit] =
    action(parse.empty) { implicit request =>
      val v = for {
          membership <- accessService.getMembership(request.authInfo.userId, membershipId)
          dto        <- membershipToDto(request.authInfo.userId, membership)
        } yield dto
      validationReply(v)
    }

  def listMemberships(): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      validationReply(
        Future {
          for {
            pagedQuery  <- PagedQuery.create(request.rawQueryString, PageSizeMax)
            memberships <- accessService.getMemberships(request.authInfo.userId,
                                                        pagedQuery.filter,
                                                        pagedQuery.sort)
            validPage   <- pagedQuery.validPage(memberships.size)
            dtos        <- {
              memberships.map(membership => membershipToDto(request.authInfo.userId, membership))
                .toList.sequenceU.map(_.toSeq)
            }
            results     <- PagedResults.create(dtos, pagedQuery.page, pagedQuery.limit)
          } yield results
        }
      )
    }

  def roleAdd(): Action[JsValue] =
    commandAction[AddRoleCmd](JsNull)(processRoleCommand)

  def roleUpdateName(roleId: AccessItemId): Action[JsValue] =
    commandAction[RoleUpdateNameCmd](Json.obj("roleId" -> roleId))(processRoleCommand)

  def roleUpdateDescription(roleId: AccessItemId): Action[JsValue] = {
    val json = Json.obj("roleId" -> roleId)
    commandAction[RoleUpdateDescriptionCmd](json)(processRoleCommand)
  }

  def roleAddUser(roleId: AccessItemId): Action[JsValue] =
    commandAction[RoleAddUserCmd](Json.obj("roleId" -> roleId))(processRoleCommand)

  def roleAddParent(roleId: AccessItemId): Action[JsValue] =
    commandAction[RoleAddParentCmd](Json.obj("roleId" -> roleId))(processRoleCommand)

  def roleAddChild(roleId: AccessItemId): Action[JsValue] =
    commandAction[RoleAddChildCmd](Json.obj("roleId" -> roleId))(processRoleCommand)

  def roleRemoveUser(roleId: AccessItemId, version: Long, userId: UserId): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      val cmd = RoleRemoveUserCmd(sessionUserId   = request.authInfo.userId.id,
                                  expectedVersion = version,
                                  roleId          = roleId.id,
                                  userId          = userId.id)
      processRoleCommand(cmd)
    }

  def roleRemoveParent(roleId: AccessItemId, version: Long, parentId: AccessItemId): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      val cmd = RoleRemoveParentCmd(sessionUserId   = request.authInfo.userId.id,
                                    expectedVersion = version,
                                    roleId          = roleId.id,
                                    parentRoleId    = parentId.id)
      processRoleCommand(cmd)
    }

  def roleRemoveChild(roleId: AccessItemId, version: Long, childId: AccessItemId): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      val cmd = RoleRemoveChildCmd(sessionUserId   = request.authInfo.userId.id,
                                   expectedVersion = version,
                                   roleId          = roleId.id,
                                   childRoleId     = childId.id)
      processRoleCommand(cmd)
    }

  def roleRemove(roleId: AccessItemId, version: Long) : Action[Unit] =
    action.async(parse.empty) { implicit request =>
      val cmd = RemoveRoleCmd(request.authInfo.userId.id, version, roleId.id)
      val future = accessService.processRemoveRoleCommand(cmd)
      validationReply(future)
    }

  def membershipAdd(): Action[JsValue] =
    commandAction[AddMembershipCmd](JsNull)(processMembershipCommand)

  def membershipUpdateName(membershipId: MembershipId): Action[JsValue] =
    commandAction[MembershipUpdateNameCmd](Json.obj("membershipId" -> membershipId))(processMembershipCommand)

  def membershipUpdateDescription(membershipId: MembershipId): Action[JsValue] = {
    val json = Json.obj("membershipId" -> membershipId)
    commandAction[MembershipUpdateDescriptionCmd](json)(processMembershipCommand)
  }

  def membershipAddUser(membershipId: MembershipId): Action[JsValue] =
    commandAction[MembershipAddUserCmd](Json.obj("membershipId" -> membershipId))(processMembershipCommand)

  def membershipAllStudies(membershipId: MembershipId): Action[JsValue] =
    commandAction[MembershipAllStudiesCmd](Json.obj("membershipId" -> membershipId))(processMembershipCommand)

  def membershipAddStudy(membershipId: MembershipId): Action[JsValue] =
    commandAction[MembershipAddStudyCmd](Json.obj("membershipId" -> membershipId))(processMembershipCommand)

  def membershipAllCentres(membershipId: MembershipId): Action[JsValue] =
    commandAction[MembershipAllCentresCmd](Json.obj("membershipId" -> membershipId))(processMembershipCommand)

  def membershipAddCentre(membershipId: MembershipId): Action[JsValue] =
    commandAction[MembershipAddCentreCmd](Json.obj("membershipId" -> membershipId))(processMembershipCommand)

  def membershipRemove(membershipId: MembershipId, version: Long) : Action[Unit] =
    action.async(parse.empty) { implicit request =>
      val cmd = RemoveMembershipCmd(request.authInfo.userId.id, version, membershipId.id)
      val future = accessService.processRemoveMembershipCommand(cmd)
      validationReply(future)
    }

  def membershipRemoveUser(membershipId: MembershipId, version: Long, userId: UserId): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      val cmd = MembershipRemoveUserCmd(sessionUserId   = request.authInfo.userId.id,
                                        expectedVersion = version,
                                        membershipId    = membershipId.id,
                                        userId          = userId.id)
      processMembershipCommand(cmd)
    }

  def membershipRemoveStudy(membershipId: MembershipId, version: Long, studyId: StudyId): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      val cmd = MembershipRemoveStudyCmd(sessionUserId   = request.authInfo.userId.id,
                                         expectedVersion = version,
                                         membershipId    = membershipId.id,
                                         studyId         = studyId.id)
      processMembershipCommand(cmd)
    }

  def membershipRemoveCentre(membershipId: MembershipId, version: Long, centreId: CentreId): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      val cmd = MembershipRemoveCentreCmd(sessionUserId   = request.authInfo.userId.id,
                                          expectedVersion = version,
                                          membershipId    = membershipId.id,
                                          centreId        = centreId.id)
      processMembershipCommand(cmd)
    }

  private def processRoleCommand(cmd: AccessCommand): Future[Result] = {
    val future = accessService.processRoleCommand(cmd).map { validation =>
        validation.flatMap(role => roleToDto(UserId(cmd.sessionUserId), role))
      }
    validationReply(future)
  }

  private def processMembershipCommand(cmd: MembershipCommand): Future[Result] = {
    val future = accessService.processMembershipCommand(cmd).map { validation =>
        validation.flatMap(membership => membershipToDto(UserId(cmd.sessionUserId), membership))
      }
    validationReply(future)
  }

  private def getUsers(ids: Set[UserId]): ControllerValidation[Set[User]] = {
    ids
      .map(usersService.getUser)
      .toList.sequenceU
      .leftMap(err => org.biobank.CommonValidations.InternalServerError.nel)
      .map(_.toSet)
  }

  private def roleToDto(requestUserId: UserId,
                        role:          Role): ControllerValidation[RoleDto] = {

    def getAccessItems(ids: Set[AccessItemId]): ControllerValidation[Set[AccessItem]] = {
      ids
        .map { id => accessService.getAccessItem(requestUserId, id) }
        .toList.sequenceU
        .leftMap(err => org.biobank.CommonValidations.InternalServerError.nel)
        .map(_.toSet)
    }

    for {
      users    <- getUsers(role.userIds)
      parents  <- getAccessItems(role.parentIds)
      children <- getAccessItems(role.childrenIds)
    } yield {
      RoleDto(id             = role.id.id,
              version        = role.version,
              timeAdded      = role.timeAdded.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
              timeModified   = role.timeModified.map(_.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)),
              accessItemType = role.accessItemType.id,
              name           = role.name,
              description    = role.description,
              userData       = entityInfoDto(users),
              parentData     = entityInfoDto(parents),
              childData      = entityInfoDto(children))
    }
  }

  private def membershipToDto(requestUserId: UserId,
                              membership:    Membership): ControllerValidation[MembershipDto] ={
    for {
      users <- getUsers(membership.userIds)
      studies <- {
        membership.studyData.ids
          .map(id => studiesService.getStudy(requestUserId, id))
          .toList.sequenceU
          .map(_.toSet)
      }
      centres <- {
        membership.centreData.ids
          .map(centresService.getCentre(requestUserId, _))
          .toList.sequenceU
          .map(_.toSet)
      }
    } yield {
      val userData        = entityInfoDto(users)
      val studyEntitySet  = entitySetDto(membership.studyData.allEntities, studies)
      val centreEntitySet = entitySetDto(membership.centreData.allEntities, centres)

      MembershipDto(id           = membership.id.id,
                    version      = membership.version,
                    timeAdded    = membership.timeAdded.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                    timeModified = membership.timeModified.map(_.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)),
                    name         = membership.name,
                    description  = membership.description,
                    userData     = userData,
                    studyData    = studyEntitySet,
                    centreData   = centreEntitySet)
    }
  }

  private def entityInfoDto[T <: ConcurrencySafeEntity[_] with HasUniqueName]
    (entities: Set[T]): Set[EntityInfoDto] = {
    entities.map { entity => EntityInfoDto(entity.id.toString, entity.name) }
  }

  private def entitySetDto[T <: ConcurrencySafeEntity[_] with HasUniqueName]
    (hasAllEntities: Boolean, entities: Set[T]): EntitySetDto = {
    EntitySetDto(hasAllEntities, entityInfoDto(entities))
  }


}
