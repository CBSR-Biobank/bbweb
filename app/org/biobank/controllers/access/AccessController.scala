package org.biobank.controllers.access

import javax.inject.{Inject, Singleton}
import org.biobank.controllers._
import org.biobank.domain.Slug
import org.biobank.domain.access._
import org.biobank.domain.centres.CentreId
import org.biobank.domain.studies.StudyId
import org.biobank.domain.users.UserId
import org.biobank.dto.access._
import org.biobank.services.PagedResults
import org.biobank.services.access.AccessService
import org.biobank.services.centres.CentresService
import org.biobank.services.studies.StudiesService
import org.biobank.services.users.UsersService
import play.api.Logger
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc._
import play.api.{Environment, Logger}
import scala.concurrent.{ExecutionContext, Future}
import scalaz.Scalaz._

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

  import org.biobank.infrastructure.commands.AccessCommands._
  import org.biobank.infrastructure.commands.MembershipCommands._
  import PagedResults._

  val log: Logger = Logger(this.getClass)

  private val PageSizeMax = 20

  def listItemNames: Action[Unit] =
    action.async(parse.empty) { implicit request =>
      FilterAndSortQueryHelper(request.rawQueryString).fold(
        err => {
          validationReply(Future.successful(err.failure[Seq[AccessItemNameDto]]))
        },
        query => {
          validationReply(accessService.getAccessItems(request.authInfo.userId, query.filter, query.sort))
        }
      )
    }

  def listRoles: Action[Unit] =
    action.async(parse.empty) { implicit request =>
      PagedQueryHelper(request.rawQueryString, PageSizeMax).fold(
        err => {
          validationReply(Future.successful(err.failure[PagedResults[RoleDto]]))
        },
        pagedQuery => {
          validationReply(accessService.getRoles(request.authInfo.userId, pagedQuery))
        }
      )
    }

  def listRoleNames: Action[Unit] =
    action.async(parse.empty) { implicit request =>
      FilterAndSortQueryHelper(request.rawQueryString).fold(
        err => {
          validationReply(Future.successful(err.failure[Seq[AccessItemNameDto]]))
        },
        query => {
          validationReply(accessService.getRoleNames(request.authInfo.userId, query))
        }
      )
    }

  def getRoleBySlug(slug: Slug): Action[Unit] =
    action(parse.empty) { implicit request =>
      val v = accessService.getRoleBySlug(request.authInfo.userId, slug)
      validationReply(v)
    }

  def getMembershipBySlug(slug: Slug): Action[Unit] =
    action(parse.empty) { implicit request =>
      val v = accessService.getMembershipBySlug(request.authInfo.userId, slug)
      validationReply(v)
    }

  def listMemberships(): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      PagedQueryHelper(request.rawQueryString, PageSizeMax).fold(
        err => {
          validationReply(Future.successful(err.failure[PagedResults[MembershipDto]]))
        },
        pagedQuery => {
          validationReply(accessService.getMemberships(request.authInfo.userId, pagedQuery))
        }
      )
    }

  def listMembershipNames: Action[Unit] =
    action.async(parse.empty) { implicit request =>
      FilterAndSortQueryHelper(request.rawQueryString).fold(
        err => {
          validationReply(Future.successful(err.failure[Seq[AccessItemNameDto]]))
        },
        query => {
          validationReply(accessService.getMembershipNames(request.authInfo.userId, query))
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

  def membershipUpdateStudyData(membershipId: MembershipId): Action[JsValue] =
    commandAction[MembershipUpdateStudyDataCmd](Json.obj("membershipId" -> membershipId))(processMembershipCommand)

  def membershipAllStudies(membershipId: MembershipId): Action[JsValue] =
    commandAction[MembershipAllStudiesCmd](Json.obj("membershipId" -> membershipId))(processMembershipCommand)

  def membershipAddStudy(membershipId: MembershipId): Action[JsValue] =
    commandAction[MembershipAddStudyCmd](Json.obj("membershipId" -> membershipId))(processMembershipCommand)

  def membershipUpdateCentreData(membershipId: MembershipId): Action[JsValue] =
    commandAction[MembershipUpdateCentreDataCmd](Json.obj("membershipId" -> membershipId))(processMembershipCommand)

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

  def snapshot: Action[Unit] =
    action(parse.empty) { implicit request =>
      validationReply(accessService.snapshotRequest(request.authInfo.userId).map { _ => true })
    }

  private def processRoleCommand(cmd: AccessCommand): Future[Result] = {
    val future = accessService.processRoleCommand(cmd)
    validationReply(future)
  }

  private def processMembershipCommand(cmd: MembershipCommand): Future[Result] = {
    val future = accessService.processMembershipCommand(cmd)
    validationReply(future)
  }

}
