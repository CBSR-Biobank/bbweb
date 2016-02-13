package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.service.AuthToken
import org.biobank.service.users.UsersService
import org.biobank.service.study.StudiesService

import javax.inject.{Inject => javaxInject, Singleton}
import scala.concurrent.Future
import scala.language.postfixOps
import play.api.Logger
import play.api.libs.json._
import scala.language.reflectiveCalls

@Singleton
class CeventTypeController @javaxInject() (val authToken:      AuthToken,
                                           val usersService:   UsersService,
                                           val studiesService: StudiesService)
    extends CommandController
    with JsonController {

  def get(studyId: String, ceventTypeId: Option[String]) =
    AuthAction(parse.empty) { (token, userId, request) =>
      Logger.debug(s"CeventTypeController.list: studyId: $studyId, ceventTypeId: $ceventTypeId")

      ceventTypeId.fold {
        domainValidationReply(studiesService.collectionEventTypesForStudy(studyId).map(_.toList))
      } { id =>
        domainValidationReply(studiesService.collectionEventTypeWithId(studyId, id))
      }
    }

  def add(studyId: String) =
    commandAction(Json.obj("studyId" -> studyId)) {
      cmd: AddCollectionEventTypeCmd => processCommand(cmd) }

  def remove(studyId: String, id: String, ver: Long) =
    AuthActionAsync(parse.empty) { (token, userId, request) =>
      val cmd = RemoveCollectionEventTypeCmd(Some(userId.id), studyId, id, ver)
      val future = studiesService.processRemoveCollectionEventTypeCommand(cmd)
      domainValidationReply(future)
  }

  def updateName(id: String) =
    commandAction(Json.obj("id" -> id)) {
      cmd: UpdateCollectionEventTypeNameCmd => processCommand(cmd) }

  def updateDescription(id: String) =
    commandAction(Json.obj("id" -> id)) {
      cmd: UpdateCollectionEventTypeDescriptionCmd => processCommand(cmd) }

  def updateRecurring(id: String) =
    commandAction(Json.obj("id" -> id)) {
      cmd: UpdateCollectionEventTypeRecurringCmd => processCommand(cmd) }

  def addAnnotationType(id: String) =
    commandAction(Json.obj("id" -> id)) {
        cmd: AddCollectionEventTypeAnnotationTypeCmd => processCommand(cmd) }

  def removeAnnotationType(studyId: String, id: String, ver: Long, uniqueId: String) =
    AuthActionAsync(parse.empty) { (token, userId, request) =>
      val cmd = RemoveCollectionEventTypeAnnotationTypeCmd(
          userId                = Some(userId.id),
          studyId               = studyId,
          id                    = id,
          expectedVersion       = ver,
          uniqueId              = uniqueId)
      processCommand(cmd)
  }

  def addSpecimenSpec(id: String) =
    commandAction(Json.obj("id" -> id)) {
      cmd: AddCollectionSpecimenSpecCmd => processCommand(cmd) }

  def removeSpecimenSpec(studyId: String, id: String, ver: Long, uniqueId: String) =
    AuthActionAsync(parse.empty) { (token, userId, request) =>
      val cmd = RemoveCollectionSpecimenSpecCmd(
          userId                = Some(userId.id),
          studyId               = studyId,
          id                    = id,
          expectedVersion       = ver,
          uniqueId              = uniqueId)
      processCommand(cmd)
  }

  private def processCommand(cmd: StudyCommand) = {
    val future = studiesService.processCollectionEventTypeCommand(cmd)
    domainValidationReply(future)
  }
}
