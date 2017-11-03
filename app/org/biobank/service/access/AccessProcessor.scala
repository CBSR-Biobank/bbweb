package org.biobank.service.access

import akka.actor._
import akka.event.{Logging, LoggingAdapter}
import akka.persistence.{RecoveryCompleted, SnapshotOffer, SaveSnapshotSuccess, SaveSnapshotFailure}
import com.github.ghik.silencer.silent
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import org.biobank.domain.access._
import org.biobank.domain.centre.CentreId
import org.biobank.domain.study.StudyId
import org.biobank.domain.user.UserId
import org.biobank.infrastructure.command.AccessCommands._
import org.biobank.infrastructure.event.AccessEvents._
import org.biobank.service.{Processor, ServiceError, ServiceValidation, SnapshotWriter}
import play.api.libs.json._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._
import scalaz._

object AccessProcessor {

  def props: Props = Props[AccessProcessor]

  final case class SnapshotState(accessItems: Set[AccessItem], memberships: Set[Membership])

  implicit val snapshotStateFormat: Format[SnapshotState] = Json.format[SnapshotState]

}

/**
 * Handles commands related to access.
 */
class AccessProcessor @Inject() (val accessItemRepository: AccessItemRepository,
                                 val membershipRepository: MembershipRepository,
                                 val snapshotWriter:       SnapshotWriter)
    extends Processor {

  import AccessProcessor._

  override val log: LoggingAdapter = Logging(context.system, this)

  override def persistenceId: String = "access-processor-id"

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  val receiveRecover: Receive = {

    case event: AccessEvent =>
      event.eventType match {
        case _: AccessEvent.EventType.Role =>
          event.getRole.eventType match {
            case _: AccessEvent.Role.EventType.UserIdAdded => applyUserAddedToRoleEvent(event)
            case _ => log.error(s"role event not handled: $event")
          }

        case _: AccessEvent.EventType.Membership =>
          event.getMembership.eventType match {
            case _: AccessEvent.Membership.EventType.Added              => applyAddedEvent(event)
            case _: AccessEvent.Membership.EventType.NameUpdated        => applyNameUpdatedEvent(event)
            case _: AccessEvent.Membership.EventType.DescriptionUpdated => applyDescriptionUpdatedEvent(event)
            case _: AccessEvent.Membership.EventType.UserAdded          => applyUserAddedEvent(event)
            case _: AccessEvent.Membership.EventType.AllStudies         => applyAllStudiesEvent(event)
            case _: AccessEvent.Membership.EventType.AllCentres         => applyAllCentresEvent(event)
            case _: AccessEvent.Membership.EventType.StudyAdded         => applyStudyAddedEvent(event)
            case _: AccessEvent.Membership.EventType.CentreAdded        => applyCentreAddedEvent(event)
            case _: AccessEvent.Membership.EventType.UserRemoved        => applyUserRemovedEvent(event)
            case _: AccessEvent.Membership.EventType.StudyRemoved       => applyStudyRemovedEvent(event)
            case _: AccessEvent.Membership.EventType.CentreRemoved      => applyCentreRemovedEvent(event)
            case _: AccessEvent.Membership.EventType.Removed            => applyMembershipRemovedEvent(event)
            case _ => log.error(s"membership event not handled: $event")
          }

        case _ => log.error(s"access event not handled: $event")
      }

    case SnapshotOffer(_, snapshotFilename: String) =>
      applySnapshot(snapshotFilename)

    case RecoveryCompleted =>
      log.debug(s"AccessProcessor: recovery completed")

    case event => log.error(s"event not handled: $event")
  }

  @SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.Throw"))
  val receiveCommand: Receive = {
    case accessCommand: AccessCommand =>
      accessCommand match {
        case cmd: AddUserToRoleCmd =>
          processUpdateRoleCmd(cmd, addUserToRoleCmdToEvent, applyUserAddedToRoleEvent)
        case cmd: AddMembershipCmd =>
          process(addMembershipCmdToEvent(cmd))(applyAddedEvent)
        case cmd: MembershipUpdateNameCmd =>
          processUpdateMembershipCmd(cmd, updateNameCmdToEvent, applyNameUpdatedEvent)
        case cmd: MembershipUpdateDescriptionCmd =>
          processUpdateMembershipCmd(cmd, updateDescriptionCmdToEvent, applyDescriptionUpdatedEvent)
        case cmd: MembershipAddUserCmd =>
          processUpdateMembershipCmd(cmd, addUserCmdToEvent, applyUserAddedEvent)
        case cmd: MembershipAllStudiesCmd =>
          processUpdateMembershipCmd(cmd, allStudiesCmdToEvent, applyAllStudiesEvent)
        case cmd: MembershipAllCentresCmd =>
          processUpdateMembershipCmd(cmd, allCentresCmdToEvent, applyAllCentresEvent)
        case cmd: MembershipAddStudyCmd =>
          processUpdateMembershipCmd(cmd, addStudyCmdToEvent, applyStudyAddedEvent)
        case cmd: MembershipAddCentreCmd =>
          processUpdateMembershipCmd(cmd, addCentreCmdToEvent, applyCentreAddedEvent)
        case cmd: MembershipRemoveUserCmd =>
          processUpdateMembershipCmd(cmd, removeUserCmdToEvent, applyUserRemovedEvent)
        case cmd: MembershipRemoveStudyCmd =>
          processUpdateMembershipCmd(cmd, removeStudyCmdToEvent, applyStudyRemovedEvent)
        case cmd: MembershipRemoveCentreCmd =>
          processUpdateMembershipCmd(cmd, removeCentreCmdToEvent, applyCentreRemovedEvent)
        case cmd: RemoveMembershipCmd =>
          processUpdateMembershipCmd(cmd, removeMembershipCmdToEvent, applyMembershipRemovedEvent)
      }

    case "persistence_restart" =>
      throw new Exception("Intentionally throwing exception to test persistence by restarting the actor")

    case "snap" =>
      mySaveSnapshot

    case SaveSnapshotSuccess(metadata) =>
      log.info(s"SaveSnapshotSuccess: $metadata")

    case SaveSnapshotFailure(metadata, reason) =>
      log.info(s"SaveSnapshotFailure: $metadata, reason: $reason")
      reason.printStackTrace

    case cmd => log.error(s"accessProcessor: message not handled: $cmd")
  }

  private def mySaveSnapshot(): Unit = {
    val snapshotState = SnapshotState(accessItemRepository.getValues.toSet,
                                      membershipRepository.getValues.toSet)
    val filename = snapshotWriter.save(persistenceId, Json.toJson(snapshotState).toString)
    saveSnapshot(filename)
  }

  private def applySnapshot(filename: String): Unit = {
    log.info(s"snapshot recovery file: $filename")
    val fileContents = snapshotWriter.load(filename);
    Json.parse(fileContents).validate[SnapshotState].fold(
      errors => log.error(s"could not apply snapshot: $filename: $errors"),
      snapshot =>  {
        log.info(s"snapshot contains ${snapshot.accessItems.size} accessItems and ${snapshot.memberships.size} memberships")
        snapshot.accessItems.foreach(accessItemRepository.put)
        snapshot.memberships.foreach(membershipRepository.put)
      }
    )
  }

  private def processUpdateRoleCmd[T <: RoleModifyCommand]
    (cmd:           T,
     validateCmd:   (T, Role) => ServiceValidation[AccessEvent],
     applyEvent:    AccessEvent => Unit): Unit = {
    val event = for {
        role         <- accessItemRepository.getRole(AccessItemId(cmd.roleId))
        validVersion <- role.requireVersion(cmd.expectedVersion)
        event        <- validateCmd(cmd, role)
      } yield event

    process(event)(applyEvent)
  }

  // userId is assumed to be valid, it should have been validated by the service
  @silent private def addUserToRoleCmdToEvent(cmd: AddUserToRoleCmd, role: Role)
      : ServiceValidation[AccessEvent] = {
    accessItemRepository.getRole(AccessItemId(cmd.roleId)).map { role =>
      AccessEvent(cmd.sessionUserId).update(
        _.time             := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        _.role.id          := cmd.roleId,
        _.role.version     := cmd.expectedVersion,
        _.role.userIdAdded := cmd.userId
      )
    }
  }

  private def addMembershipCmdToEvent(cmd: AddMembershipCmd): ServiceValidation[AccessEvent] = {
    for {
      name          <- nameAvailable(cmd.name)
      membershipId  <- validNewIdentity(membershipRepository.nextIdentity, membershipRepository)
      newMembership <- Membership.create(id           = membershipId,
                                         version      = 0L,
                                         timeAdded    = OffsetDateTime.now,
                                         timeModified = None,
                                         name         = cmd.name,
                                         description  = None,
                                         userIds      = cmd.userIds.map(id => UserId(id)).toSet,
                                         allStudies   = cmd.allStudies,
                                         allCentres   = cmd.allCentres,
                                         studyIds     = cmd.studyIds.map(id => StudyId(id)).toSet,
                                         centreIds    = cmd.centreIds.map(id => CentreId(id)).toSet)
    } yield AccessEvent(cmd.sessionUserId).update(
      _.time                                 := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
      _.membership.id                        := membershipId.id,
      _.membership.added.name                := cmd.name,
      _.membership.added.optionalDescription := cmd.description,
      _.membership.added.userIds             := cmd.userIds,
      _.membership.added.allStudies          := cmd.allStudies,
      _.membership.added.studyIds            := cmd.studyIds,
      _.membership.added.allCentres          := cmd.allCentres,
      _.membership.added.centreIds           := cmd.centreIds);

  }

  private def updateNameCmdToEvent(cmd: MembershipUpdateNameCmd, membership: Membership)
      : ServiceValidation[AccessEvent] = {
    (nameAvailable(cmd.name, MembershipId(cmd.membershipId)) |@|
       membership.withName(cmd.name)) { case (_, _) =>
        AccessEvent(cmd.sessionUserId).update(
          _.time                           := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
          _.membership.id                  := membership.id.id,
          _.membership.nameUpdated.version := cmd.expectedVersion,
          _.membership.nameUpdated.name    := cmd.name)
    }
  }

  private def updateDescriptionCmdToEvent(cmd: MembershipUpdateDescriptionCmd, membership: Membership)
      : ServiceValidation[AccessEvent] = {
    membership.withDescription(cmd.description).map { _ =>
      val timeStr = OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
      AccessEvent(cmd.sessionUserId).update(
        _.time                                              := timeStr,
        _.membership.id                                     := membership.id.id,
        _.membership.descriptionUpdated.version             := cmd.expectedVersion,
        _.membership.descriptionUpdated.optionalDescription := cmd.description)
    }
  }

  private def addUserCmdToEvent(cmd: MembershipAddUserCmd, membership: Membership)
      : ServiceValidation[AccessEvent] = {
    val userId = UserId(cmd.userId)
    if (membership.userIds.exists(_ == userId)) {
      ServiceError(s"user ID is already in membership: ${userId}").failureNel[AccessEvent]
    } else {
      AccessEvent(cmd.sessionUserId).update(
        _.time                         := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        _.membership.id                := membership.id.id,
        _.membership.userAdded.version := cmd.expectedVersion,
        _.membership.userAdded.id      := cmd.userId
      ).successNel[String]
    }
  }

  private def allStudiesCmdToEvent(cmd: MembershipAllStudiesCmd, membership: Membership)
      : ServiceValidation[AccessEvent] = {
    AccessEvent(cmd.sessionUserId).update(
      _.time                          := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
      _.membership.id                 := membership.id.id,
      _.membership.allStudies.version := cmd.expectedVersion,
    ).successNel[String]
  }

  private def allCentresCmdToEvent(cmd: MembershipAllCentresCmd, membership: Membership)
      : ServiceValidation[AccessEvent] = {
    AccessEvent(cmd.sessionUserId).update(
      _.time                          := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
      _.membership.id                 := membership.id.id,
      _.membership.allCentres.version := cmd.expectedVersion,
    ).successNel[String]
  }

  private def addStudyCmdToEvent(cmd: MembershipAddStudyCmd, membership: Membership)
      : ServiceValidation[AccessEvent] = {
    val studyId = StudyId(cmd.studyId)
    if (membership.studyData.ids.exists(_ == studyId)) {
      ServiceError(s"study ID is already in membership: ${studyId}").failureNel[AccessEvent]
    } else {
      AccessEvent(cmd.sessionUserId).update(
        _.time                          := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        _.membership.id                 := membership.id.id,
        _.membership.studyAdded.version := cmd.expectedVersion,
        _.membership.studyAdded.id      := cmd.studyId
      ).successNel[String]
    }
  }

  private def addCentreCmdToEvent(cmd: MembershipAddCentreCmd, membership: Membership)
      : ServiceValidation[AccessEvent] = {
    val centreId = CentreId(cmd.centreId)
    if (membership.centreData.ids.exists(_ == centreId)) {
      ServiceError(s"centre ID is already in membership: ${centreId}").failureNel[AccessEvent]
    } else {
      AccessEvent(cmd.sessionUserId).update(
        _.time                           := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        _.membership.id                  := membership.id.id,
        _.membership.centreAdded.version := cmd.expectedVersion,
        _.membership.centreAdded.id      := cmd.centreId
      ).successNel[String]
    }
  }

  private def removeUserCmdToEvent(cmd: MembershipRemoveUserCmd, membership: Membership)
      : ServiceValidation[AccessEvent] = {
    val userId = UserId(cmd.userId)
    if (!membership.userIds.exists(_ == userId)) {
      ServiceError(s"user ID is not in membership: ${userId}").failureNel[AccessEvent]
    } else {
      AccessEvent(cmd.sessionUserId).update(
        _.time                           := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        _.membership.id                  := membership.id.id,
        _.membership.userRemoved.version := cmd.expectedVersion,
        _.membership.userRemoved.id      := cmd.userId
      ).successNel[String]
    }
  }

  private def removeStudyCmdToEvent(cmd: MembershipRemoveStudyCmd, membership: Membership)
      : ServiceValidation[AccessEvent] = {
    val studyId = StudyId(cmd.studyId)
    if (membership.studyData.allEntities) {
      ServiceError(s"membership is for all studies, cannot remove: ${studyId}").failureNel[AccessEvent]
    } else if (!membership.studyData.ids.exists(_.id == cmd.studyId)) {
      ServiceError(s"study ID is not in membership: ${studyId}").failureNel[AccessEvent]
    } else {
      AccessEvent(cmd.sessionUserId).update(
        _.time                            := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        _.membership.id                   := membership.id.id,
        _.membership.studyRemoved.version := cmd.expectedVersion,
        _.membership.studyRemoved.id      := cmd.studyId
      ).successNel[String]
    }
  }

  private def removeCentreCmdToEvent(cmd: MembershipRemoveCentreCmd, membership: Membership)
      : ServiceValidation[AccessEvent] = {
    val centreId = CentreId(cmd.centreId)
    if (membership.centreData.allEntities) {
      ServiceError(s"membership is for all centres, cannot remove: ${centreId}").failureNel[AccessEvent]
    } else if (!membership.centreData.ids.exists(_ == centreId)) {
      ServiceError(s"centre ID is not in membership: ${centreId}").failureNel[AccessEvent]
    } else {
      AccessEvent(cmd.sessionUserId).update(
        _.time                             := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        _.membership.id                    := membership.id.id,
        _.membership.centreRemoved.version := cmd.expectedVersion,
        _.membership.centreRemoved.id      := cmd.centreId
      ).successNel[String]
    }
  }

  private def removeMembershipCmdToEvent(cmd: RemoveMembershipCmd, membership: Membership)
      : ServiceValidation[AccessEvent] = {
    AccessEvent(cmd.sessionUserId).update(
       _.time                      := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
      _.membership.id              := membership.id.id,
      _.membership.removed.version := cmd.expectedVersion
    ).successNel[String]
}

  private def onValidRoleEvent(event: AccessEvent)
                              (applyEvent: (Role, OffsetDateTime) => ServiceValidation[Boolean])
      : Unit = {
    if (!event.eventType.isRole) {
      log.error(s"invalid role event type: $event")
    } else {
      accessItemRepository.getRole(AccessItemId(event.getRole.getId)).fold(
        err => log.error(s"role from event does not exist: $err"),
        role => {
          if (role.version != event.getRole.getVersion) {
            log.error(s"event version check failed: role version: ${role.version}, event: $event")
          } else {
            val eventTime = OffsetDateTime.parse(event.getTime)
            val update = applyEvent(role, eventTime)

            if (update.isFailure) {
              log.error(s"role update from event failed: $update")
            }
          }
        }
      )
    }
  }

  private def applyUserAddedToRoleEvent(event: AccessEvent): Unit = {
    onValidRoleEvent(event) { (role, eventTime) =>
      accessItemRepository.put(role.addUser(UserId(event.getRole.getUserIdAdded)))
      true.successNel[String]
    }
  }

  private def applyAddedEvent(event: AccessEvent): Unit = {
    if (!event.eventType.isMembership || !event.getMembership.eventType.isAdded) {
      log.error(s"applyAddedEvent: invalid event type: $event")
    } else {
      val addedEvent = event.getMembership.getAdded
      val timeAdded = OffsetDateTime.parse(event.getTime)

      val v = Membership.create(id           = MembershipId(event.getMembership.getId),
                                version      = 0L,
                                timeAdded    = timeAdded,
                                timeModified = None,
                                name         = addedEvent.getName,
                                description  = addedEvent.description,
                                userIds      = addedEvent.userIds.map(id => UserId(id)).toSet,
                                allStudies   = addedEvent.getAllStudies,
                                allCentres   = addedEvent.getAllCentres,
                                studyIds     = addedEvent.studyIds.map(id => StudyId(id)).toSet,
                                centreIds    = addedEvent.centreIds.map(id => CentreId(id)).toSet)

      if (v.isFailure) {
        log.error(s"could not add membership from event: $v")
      }

      v.foreach(membershipRepository.put)
    }
  }

  private def applyNameUpdatedEvent(event: AccessEvent): Unit = {
    onValidMembershipEventAndVersion(event,
                                     event.getMembership.eventType.isNameUpdated,
                                     event.getMembership.getNameUpdated.getVersion) {
      (membership, _, time) =>
      membership.withName(event.getMembership.getNameUpdated.getName).map { updated =>
        membershipRepository.put(updated.copy(timeModified = Some(time)))
        true
      }
    }
  }

  private def applyDescriptionUpdatedEvent(event: AccessEvent): Unit = {
    onValidMembershipEventAndVersion(event,
                                     event.getMembership.eventType.isDescriptionUpdated,
                                     event.getMembership.getDescriptionUpdated.getVersion) {
      (membership, _, time) =>
      membership.withDescription(event.getMembership.getDescriptionUpdated.description).map { updated =>
        membershipRepository.put(updated.copy(timeModified = Some(time)))
        true
      }
    }
  }

  private def applyUserAddedEvent(event: AccessEvent): Unit = {
    onValidMembershipEventAndVersion(event,
                                     event.getMembership.eventType.isUserAdded,
                                     event.getMembership.getUserAdded.getVersion) {
      (membership, _, time) =>
      val updated = membership
        .addUser(UserId(event.getMembership.getUserAdded.getId))
        .copy(timeModified = Some(time))
      membershipRepository.put(updated)
      true.successNel[String]
    }
  }

  private def applyAllStudiesEvent(event: AccessEvent): Unit = {
    onValidMembershipEventAndVersion(event,
                                     event.getMembership.eventType.isAllStudies,
                                     event.getMembership.getAllStudies.getVersion) {
      (membership, _, time) =>
      val updated = membership.hasAllStudies.copy(timeModified = Some(time))
      membershipRepository.put(updated)
      true.successNel[String]
    }
  }

  private def applyAllCentresEvent(event: AccessEvent): Unit = {
    onValidMembershipEventAndVersion(event,
                                     event.getMembership.eventType.isAllCentres,
                                     event.getMembership.getAllCentres.getVersion) {
      (membership, _, time) =>
      val updated = membership.hasAllCentres.copy(timeModified = Some(time))
      membershipRepository.put(updated)
      true.successNel[String]
    }
  }

  private def applyStudyAddedEvent(event: AccessEvent): Unit = {
    onValidMembershipEventAndVersion(event,
                                     event.getMembership.eventType.isStudyAdded,
                                     event.getMembership.getStudyAdded.getVersion) {
      (membership, _, time) =>
      val updated = membership
        .addStudy(StudyId(event.getMembership.getStudyAdded.getId))
        .copy(timeModified = Some(time))
      membershipRepository.put(updated)
      true.successNel[String]
    }
  }

  private def applyCentreAddedEvent(event: AccessEvent): Unit = {
    onValidMembershipEventAndVersion(event,
                                     event.getMembership.eventType.isCentreAdded,
                                     event.getMembership.getCentreAdded.getVersion) {
      (membership, _, time) =>
      val updated = membership
        .addCentre(CentreId(event.getMembership.getCentreAdded.getId))
        .copy(timeModified = Some(time))
      membershipRepository.put(updated)
      true.successNel[String]
    }
  }

  private def applyUserRemovedEvent(event: AccessEvent): Unit = {
    onValidMembershipEventAndVersion(event,
                                     event.getMembership.eventType.isUserRemoved,
                                     event.getMembership.getUserRemoved.getVersion) {
      (membership, _, time) =>
      val updated = membership
        .removeUser(UserId(event.getMembership.getUserRemoved.getId))
        .copy(timeModified = Some(time))
      membershipRepository.put(updated)
      true.successNel[String]
    }
  }

  private def applyStudyRemovedEvent(event: AccessEvent): Unit = {
    onValidMembershipEventAndVersion(event,
                                     event.getMembership.eventType.isStudyRemoved,
                                     event.getMembership.getStudyRemoved.getVersion) {
      (membership, _, time) =>
      val updated = membership
        .removeStudy(StudyId(event.getMembership.getStudyRemoved.getId))
        .copy(timeModified = Some(time))
      membershipRepository.put(updated)
      true.successNel[String]
    }
  }

  private def applyCentreRemovedEvent(event: AccessEvent): Unit = {
    onValidMembershipEventAndVersion(event,
                                     event.getMembership.eventType.isCentreRemoved,
                                     event.getMembership.getCentreRemoved.getVersion) {
      (membership, _, time) =>
      val updated = membership
        .removeCentre(CentreId(event.getMembership.getCentreRemoved.getId))
        .copy(timeModified = Some(time))
      membershipRepository.put(updated)
      true.successNel[String]
    }
  }

  private def applyMembershipRemovedEvent(event: AccessEvent): Unit = {
    onValidMembershipEventAndVersion(event,
                                     event.getMembership.eventType.isRemoved,
                                     event.getMembership.getRemoved.getVersion) {
      (membership, _, time) =>
      membershipRepository.remove(membership)
      true.successNel[String]
    }
  }

  private def processUpdateMembershipCmd[T <: MembershipModifyCommand]
    (cmd: T,
     cmdToEvent: (T, Membership) => ServiceValidation[AccessEvent],
     applyEvent: AccessEvent => Unit): Unit = {
    val event = for {
        membership   <- membershipRepository.getByKey(MembershipId(cmd.membershipId))
        validVersion <- membership.requireVersion(cmd.expectedVersion)
        event        <- cmdToEvent(cmd, membership)
      } yield event
    process(event)(applyEvent)
  }

  private def onValidMembershipEventAndVersion(event:        AccessEvent,
                                               eventType:    Boolean,
                                               eventVersion: Long)
                                              (applyEvent:  (Membership,
                                                             AccessEvent,
                                                             OffsetDateTime) => ServiceValidation[Boolean])
      : Unit = {
    if (!eventType) {
      log.error(s"invalid event type: $event")
    } else {
      membershipRepository.getByKey(MembershipId(event.getMembership.getId)).fold(
        err => log.error(s"membership from event does not exist: $err"),
        membership => {
          if (membership.version != eventVersion) {
            log.error(s"event version check failed: membership version: ${membership.version}, event: $event")
          } else {
            val eventTime = OffsetDateTime.parse(event.getTime)
            val update = applyEvent(membership, event, eventTime)
            if (update.isFailure) {
              log.error(s"membership update from event failed: event: $event, reason: $update")
            }
          }
        }
      )
    }
  }

  val ErrMsgNameExists: String = "name already used"

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  private def nameAvailable(name: String): ServiceValidation[Boolean] = {
    nameAvailableMatcher(name, membershipRepository, ErrMsgNameExists) { item =>
      item.name == name
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  private def nameAvailable(name: String, excludeId: MembershipId): ServiceValidation[Boolean] = {
    nameAvailableMatcher(name, membershipRepository, ErrMsgNameExists){ item =>
      (item.name == name) && (item.id != excludeId)
    }
  }

  private def init(): Unit = {
    accessItemRepository.init
    membershipRepository.init
  }

  init
}
