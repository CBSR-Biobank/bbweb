package org.biobank.controllers.study

import javax.inject.{Inject, Singleton}
import org.biobank.controllers._
import org.biobank.infrastructure.command.CollectionEventTypeCommands._
import org.biobank.service.AuthToken
import org.biobank.service.study.StudiesService
import org.biobank.service.users.UsersService
import play.api.libs.json._
import play.api.{ Environment, Logger }

@Singleton
class CeventTypeController @Inject() (val env:            Environment,
                                      val authToken:      AuthToken,
                                      val usersService:   UsersService,
                                      val studiesService: StudiesService)
    extends CommandController
    with JsonController {

  def get(studyId: String, ceventTypeId: Option[String]) =
    AuthAction(parse.empty) { (token, userId, request) =>
      Logger.debug(s"CeventTypeController.list: studyId: $studyId, ceventTypeId: $ceventTypeId")

      ceventTypeId.fold {
        validationReply(studiesService.collectionEventTypesForStudy(studyId).map(_.toList))
      } { id =>
        validationReply(studiesService.collectionEventTypeWithId(studyId, id))
      }
    }

  def add(studyId: String) =
    commandAction(Json.obj("studyId" -> studyId)) {
      cmd: AddCollectionEventTypeCmd => processCommand(cmd) }

  def remove(studyId: String, id: String, ver: Long) =
    AuthActionAsync(parse.empty) { (token, userId, request) =>
      val cmd = RemoveCollectionEventTypeCmd(Some(userId.id), studyId, id, ver)
      val future = studiesService.processRemoveCollectionEventTypeCommand(cmd)
      validationReply(future)
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
        cmd: CollectionEventTypeAddAnnotationTypeCmd => processCommand(cmd) }

  def updateAnnotationType(id: String, uniqueId: String) =
    commandAction(Json.obj("id" -> id, "uniqueId" -> uniqueId)) {
        cmd: CollectionEventTypeUpdateAnnotationTypeCmd => processCommand(cmd) }

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

  def updateSpecimenSpec(id: String, uniqueId: String) =
    commandAction(Json.obj("id" -> id, "uniqueId" -> uniqueId)) {
      cmd: UpdateCollectionSpecimenSpecCmd => processCommand(cmd) }

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

  def inUse(id: String) =
    AuthAction(parse.empty) { (token, userId, request) =>
      validationReply(studiesService.collectionEventTypeInUse(id))
    }

  private def processCommand(cmd: CollectionEventTypeCommand) = {
    val future = studiesService.processCollectionEventTypeCommand(cmd)
    validationReply(future)
  }
}
