package org.biobank.controllers.access

import javax.inject.{Inject, Singleton}
import org.biobank.controllers._
import org.biobank.domain.access._
import org.biobank.domain.access.RoleId._
import org.biobank.domain.centre.CentreId
import org.biobank.domain.study.StudyId
import org.biobank.domain.user.UserId
import org.biobank.service.PagedResults
import org.biobank.service.studies.StudiesService
import org.biobank.service.access.AccessService
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
                                  val action:         BbwebAction,
                                  val env:            Environment,
                                  val accessService:  AccessService,
                                  val studiesService: StudiesService)
                              (implicit val ec: ExecutionContext)
    extends CommandController(controllerComponents) {

  import org.biobank.infrastructure.command.AccessCommands._

  val log: Logger = Logger(this.getClass)

  private val PageSizeMax = 20

  def listRoles: Action[Unit] =
    action.async(parse.empty) { implicit request =>
      validationReply(
        Future {
          for {
              pagedQuery <- PagedQuery.create(request.rawQueryString, PageSizeMax)
              access     <- accessService.getRoles(request.authInfo.userId, pagedQuery.filter, pagedQuery.sort)
              validPage  <- pagedQuery.validPage(access.size)
              results    <- PagedResults.create(access, pagedQuery.page, pagedQuery.limit)
            } yield results
        }
      )
    }

  def getRole(roleId: RoleId): Action[Unit] =
    action(parse.empty) { implicit request =>
      validationReply(accessService.getRole(request.authInfo.userId, roleId))
    }

  def getRolePermissions(roleId: RoleId): Action[Unit] =
    action(parse.empty) { implicit request =>
      validationReply(accessService.getRolePermissions(request.authInfo.userId, roleId))
    }

  def getMembership(membershipId: MembershipId): Action[Unit] =
    action(parse.empty) { implicit request =>
      validationReply(accessService.getMembership(request.authInfo.userId, membershipId))
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
            results     <- PagedResults.create(memberships, pagedQuery.page, pagedQuery.limit)
          } yield results
        }
      )
    }

  def membershipAdd() : Action[JsValue] =
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

  private def processMembershipCommand(cmd: AccessCommand): Future[Result] = {
    val future = accessService.processMembershipCommand(cmd)
    validationReply(future)
  }

}
