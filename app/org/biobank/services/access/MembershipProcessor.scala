package org.biobank.services.access

import akka.actor._
import akka.event.{Logging, LoggingAdapter}
import akka.persistence.{RecoveryCompleted, SnapshotOffer, SaveSnapshotSuccess, SaveSnapshotFailure}
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import org.biobank.domain.access._
import org.biobank.domain.centres.CentreId
import org.biobank.domain.studies.StudyId
import org.biobank.domain.users.UserId
import org.biobank.infrastructure.commands.MembershipCommands._
import org.biobank.infrastructure.events.MembershipEvents._
import org.biobank.services.{Processor, ServiceError, ServiceValidation, SnapshotWriter}
import play.api.libs.json._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._
import scalaz._

object MembershipProcessor {

  def props: Props = Props[MembershipProcessor]

  final case class SnapshotState(memberships: Set[Membership])

  implicit val snapshotStateFormat: Format[SnapshotState] = Json.format[SnapshotState]

}

/**
 * Handles commands related to membership.
 */
class MembershipProcessor @Inject() (val membershipRepository: MembershipRepository,
                                     val snapshotWriter:       SnapshotWriter)
    extends Processor {

  import MembershipProcessor._
  import org.biobank.CommonValidations._

  override val log: LoggingAdapter = Logging(context.system, this)

  override def persistenceId: String = "membership-processor-id"

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var replyTo: Option[ActorRef] = None

  @SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.Throw"))
  val receiveRecover: Receive = {
    case event: MembershipEvent =>
      event.eventType match {
        case _: MembershipEvent.EventType.Added              => applyAddedEvent(event)
        case _: MembershipEvent.EventType.NameUpdated        => applyNameUpdatedEvent(event)
        case _: MembershipEvent.EventType.DescriptionUpdated => applyDescriptionUpdatedEvent(event)
        case _: MembershipEvent.EventType.UserAdded          => applyUserAddedEvent(event)
        case _: MembershipEvent.EventType.UserRemoved        => applyUserRemovedEvent(event)

        case _: MembershipEvent.EventType.StudyDataUpdated   => applyStudyDataUpdateEvent(event)
        case _: MembershipEvent.EventType.AllStudies         => applyAllStudiesEvent(event)
        case _: MembershipEvent.EventType.StudyAdded         => applyStudyAddedEvent(event)
        case _: MembershipEvent.EventType.StudyRemoved       => applyStudyRemovedEvent(event)

        case _: MembershipEvent.EventType.CentreDataUpdated  => applyCentreDataUpdateEvent(event)
        case _: MembershipEvent.EventType.AllCentres         => applyAllCentresEvent(event)
        case _: MembershipEvent.EventType.CentreAdded        => applyCentreAddedEvent(event)
        case _: MembershipEvent.EventType.CentreRemoved      => applyCentreRemovedEvent(event)

        case _: MembershipEvent.EventType.Removed            => applyMembershipRemovedEvent(event)
        case _ => throw new Exception(s"membership event not handled: $event")
      }

    case SnapshotOffer(_, snapshotFilename: String) =>
      applySnapshot(snapshotFilename)

    case RecoveryCompleted =>
      log.debug(s"MembershipProcessor: recovery completed")

    case event => throw new Exception(s"event not handled: $event")
  }

  @SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.Throw"))
  val receiveCommand: Receive = {
    case membershipCommand: MembershipCommand =>
      membershipCommand match {
        // case cmd: AddUserToRoleCmd =>
        //   processUpdateRoleCmd(cmd, addUserToRoleCmdToEvent, applyUserAddedToRoleEvent)
        case cmd: AddMembershipCmd =>
          process(addMembershipCmdToEvent(cmd))(applyAddedEvent)
        case cmd: MembershipUpdateNameCmd =>
          processUpdateMembershipCmd(cmd, updateNameCmdToEvent, applyNameUpdatedEvent)
        case cmd: MembershipUpdateDescriptionCmd =>
          processUpdateMembershipCmd(cmd, updateDescriptionCmdToEvent, applyDescriptionUpdatedEvent)
        case cmd: MembershipAddUserCmd =>
          processUpdateMembershipCmd(cmd, addUserCmdToEvent, applyUserAddedEvent)
        case cmd: MembershipRemoveUserCmd =>
          processUpdateMembershipCmd(cmd, removeUserCmdToEvent, applyUserRemovedEvent)

        case cmd: MembershipUpdateStudyDataCmd =>
          processUpdateMembershipCmd(cmd, updateStudyDataCmdToEvent, applyStudyDataUpdateEvent)
        case cmd: MembershipAllStudiesCmd =>
          processUpdateMembershipCmd(cmd, allStudiesCmdToEvent, applyAllStudiesEvent)
        case cmd: MembershipAddStudyCmd =>
          processUpdateMembershipCmd(cmd, addStudyCmdToEvent, applyStudyAddedEvent)
        case cmd: MembershipRemoveStudyCmd =>
          processUpdateMembershipCmd(cmd, removeStudyCmdToEvent, applyStudyRemovedEvent)

        case cmd: MembershipUpdateCentreDataCmd =>
          processUpdateMembershipCmd(cmd, updateCentreDataCmdToEvent, applyCentreDataUpdateEvent)
        case cmd: MembershipAllCentresCmd =>
          processUpdateMembershipCmd(cmd, allCentresCmdToEvent, applyAllCentresEvent)
        case cmd: MembershipAddCentreCmd =>
          processUpdateMembershipCmd(cmd, addCentreCmdToEvent, applyCentreAddedEvent)
        case cmd: MembershipRemoveCentreCmd =>
          processUpdateMembershipCmd(cmd, removeCentreCmdToEvent, applyCentreRemovedEvent)

        case cmd: RemoveMembershipCmd =>
          processUpdateMembershipCmd(cmd, removeMembershipCmdToEvent, applyMembershipRemovedEvent)
      }

    case "persistence_restart" =>
      throw new Exception("Intentionally throwing exception to test persistence by restarting the actor")

    case "snap" =>
      mySaveSnapshot
      replyTo = Some(sender())

    case SaveSnapshotSuccess(metadata) =>
      log.debug(s"SaveSnapshotSuccess: $metadata")
      replyTo.foreach(_ ! akka.actor.Status.Success(s"snapshot saved: $metadata"))
      replyTo = None

    case SaveSnapshotFailure(metadata, reason) =>
      log.debug(s"SaveSnapshotFailure: $metadata, reason: $reason")
      replyTo.foreach(_ ! akka.actor.Status.Failure(reason))
      replyTo = None
  }

  private def mySaveSnapshot(): Unit = {
    val snapshotState = SnapshotState(membershipRepository.getValues.toSet)
    val filename = snapshotWriter.save(persistenceId, Json.toJson(snapshotState).toString)
    saveSnapshot(filename)
  }

  private def applySnapshot(filename: String): Unit = {
    log.debug(s"snapshot recovery file: $filename")
    val fileContents = snapshotWriter.load(filename);
    Json.parse(fileContents).validate[SnapshotState].fold(
      errors => log.error(s"could not apply snapshot: $filename: $errors"),
      snapshot =>  {
        log.debug(s"snapshot contains ${snapshot.memberships.size} memberships")
        snapshot.memberships.foreach(membershipRepository.put)
      }
    )
  }

  private def addMembershipCmdToEvent(cmd: AddMembershipCmd): ServiceValidation[MembershipEvent] = {
    for {
      name          <- nameAvailable(cmd.name)
      membershipId  <- validNewIdentity(membershipRepository.nextIdentity, membershipRepository)
      newMembership <- Membership.create(id           = membershipId,
                                        version      = 0L,
                                        timeAdded    = OffsetDateTime.now,
                                        timeModified = None,
                                        name         = cmd.name,
                                        description  = cmd.description,
                                        userIds      = cmd.userIds.map(id => UserId(id)).toSet,
                                        allStudies   = cmd.allStudies,
                                        allCentres   = cmd.allCentres,
                                        studyIds     = cmd.studyIds.map(id => StudyId(id)).toSet,
                                        centreIds    = cmd.centreIds.map(id => CentreId(id)).toSet)
    } yield MembershipEvent(cmd.sessionUserId).update(
      _.time                      := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
      _.id                        := membershipId.id,
      _.added.name                := cmd.name,
      _.added.optionalDescription := cmd.description,
      _.added.userIds             := cmd.userIds,
      _.added.allStudies          := cmd.allStudies,
      _.added.studyIds            := cmd.studyIds,
      _.added.allCentres          := cmd.allCentres,
      _.added.centreIds           := cmd.centreIds);

  }

  private def updateNameCmdToEvent(cmd: MembershipUpdateNameCmd, membership: Membership)
      : ServiceValidation[MembershipEvent] = {
    (nameAvailable(cmd.name, MembershipId(cmd.membershipId)) |@|
       membership.withName(cmd.name)) { case _ =>
        MembershipEvent(cmd.sessionUserId).update(
          _.time                := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
          _.id                  := membership.id.id,
          _.nameUpdated.version := cmd.expectedVersion,
          _.nameUpdated.name    := cmd.name)
    }
  }

  private def updateDescriptionCmdToEvent(cmd: MembershipUpdateDescriptionCmd, membership: Membership)
      : ServiceValidation[MembershipEvent] = {
    membership.withDescription(cmd.description).map { _ =>
      val timeStr = OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
      MembershipEvent(cmd.sessionUserId).update(
        _.time                                   := timeStr,
        _.id                                     := membership.id.id,
        _.descriptionUpdated.version             := cmd.expectedVersion,
        _.descriptionUpdated.optionalDescription := cmd.description)
    }
  }

  private def addUserCmdToEvent(cmd: MembershipAddUserCmd, membership: Membership)
      : ServiceValidation[MembershipEvent] = {
    val userId = UserId(cmd.userId)
    if (membership.userIds.exists(_ == userId)) {
      EntityCriteriaError(s"user ID is already in membership: ${userId}").failureNel[MembershipEvent]
    } else {
      MembershipEvent(cmd.sessionUserId).update(
        _.time              := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        _.id                := membership.id.id,
        _.userAdded.version := cmd.expectedVersion,
        _.userAdded.id      := cmd.userId
      ).successNel[String]
    }
  }

  // study IDs were already validated in service
  private def updateStudyDataCmdToEvent(cmd: MembershipUpdateStudyDataCmd, membership: Membership)
      : ServiceValidation[MembershipEvent] = {
    if (cmd.allStudies && cmd.studyIds.nonEmpty) {
      EntityCriteriaError("membership cannot be for all studies and also individual studies")
        .failureNel[MembershipEvent]
    } else if (!cmd.allStudies && cmd.studyIds.isEmpty) {
      EntityCriteriaError("membership must contain studies")
        .failureNel[MembershipEvent]
    } else {
      val subEvent = MembershipEvent.StudyDataUpdated()
        .update(_.version    := cmd.expectedVersion,
                _.allStudies := cmd.allStudies,
                _.studyIds   := cmd.studyIds)

      MembershipEvent(cmd.sessionUserId).update(
        _.time             := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        _.id               := membership.id.id,
        _.studyDataUpdated := subEvent
      ).successNel[String]
    }
  }

  private def allStudiesCmdToEvent(cmd: MembershipAllStudiesCmd, membership: Membership)
      : ServiceValidation[MembershipEvent] = {
    MembershipEvent(cmd.sessionUserId).update(
      _.time               := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
      _.id                 := membership.id.id,
      _.allStudies.version := cmd.expectedVersion,
      ).successNel[String]
  }

  // study IDs were already validated in service
  private def addStudyCmdToEvent(cmd: MembershipAddStudyCmd, membership: Membership)
      : ServiceValidation[MembershipEvent] = {
    val studyId = StudyId(cmd.studyId)
    if (membership.studyData.ids.exists(_ == studyId)) {
      ServiceError(s"study ID is already in membership: ${studyId}").failureNel[MembershipEvent]
    } else {
      MembershipEvent(cmd.sessionUserId).update(
        _.time               := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        _.id                 := membership.id.id,
        _.studyAdded.version := cmd.expectedVersion,
        _.studyAdded.id      := cmd.studyId
      ).successNel[String]
    }
  }

  // centre IDs were already validated in service
  private def updateCentreDataCmdToEvent(cmd: MembershipUpdateCentreDataCmd, membership: Membership)
      : ServiceValidation[MembershipEvent] = {
    if (cmd.allCentres && cmd.centreIds.nonEmpty) {
      EntityCriteriaError("membership cannot be for all centres and also individual centres")
        .failureNel[MembershipEvent]
    } else if (!cmd.allCentres && cmd.centreIds.isEmpty) {
      EntityCriteriaError("membership must contain centres")
        .failureNel[MembershipEvent]
    } else {
      val subEvent = MembershipEvent.CentreDataUpdated().update(
          _.version    := cmd.expectedVersion,
          _.allCentres := cmd.allCentres,
          _.centreIds  := cmd.centreIds
        )

      MembershipEvent(cmd.sessionUserId).update(
        _.time              := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        _.id                := membership.id.id,
        _.centreDataUpdated := subEvent
      ).successNel[String]
    }
  }

  private def allCentresCmdToEvent(cmd: MembershipAllCentresCmd, membership: Membership)
      : ServiceValidation[MembershipEvent] = {
    MembershipEvent(cmd.sessionUserId).update(
      _.time               := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
      _.id                 := membership.id.id,
      _.allCentres.version := cmd.expectedVersion,
      ).successNel[String]
  }

  private def addCentreCmdToEvent(cmd: MembershipAddCentreCmd, membership: Membership)
      : ServiceValidation[MembershipEvent] = {
    val centreId = CentreId(cmd.centreId)
    if (membership.centreData.ids.exists(_ == centreId)) {
      ServiceError(s"centre ID is already in membership: ${centreId}").failureNel[MembershipEvent]
    } else {
      MembershipEvent(cmd.sessionUserId).update(
        _.time                := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        _.id                  := membership.id.id,
        _.centreAdded.version := cmd.expectedVersion,
        _.centreAdded.id      := cmd.centreId
      ).successNel[String]
    }
  }

  private def removeUserCmdToEvent(cmd: MembershipRemoveUserCmd, membership: Membership)
      : ServiceValidation[MembershipEvent] = {
    val userId = UserId(cmd.userId)
    if (!membership.userIds.exists(_ == userId)) {
      EntityCriteriaError(s"user ID is not in membership: ${userId}").failureNel[MembershipEvent]
    } else {
      MembershipEvent(cmd.sessionUserId).update(
        _.time                := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        _.id                  := membership.id.id,
        _.userRemoved.version := cmd.expectedVersion,
        _.userRemoved.id      := cmd.userId
      ).successNel[String]
    }
  }

  private def removeStudyCmdToEvent(cmd: MembershipRemoveStudyCmd, membership: Membership)
      : ServiceValidation[MembershipEvent] = {
    val studyId = StudyId(cmd.studyId)
    if (membership.studyData.allEntities) {
      ServiceError(s"membership is for all studies, cannot remove: ${studyId}").failureNel[MembershipEvent]
    } else if (!membership.studyData.ids.exists(_.id == cmd.studyId)) {
      ServiceError(s"study ID is not in membership: ${studyId}").failureNel[MembershipEvent]
    } else {
      MembershipEvent(cmd.sessionUserId).update(
        _.time                 := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        _.id                   := membership.id.id,
        _.studyRemoved.version := cmd.expectedVersion,
        _.studyRemoved.id      := cmd.studyId
      ).successNel[String]
    }
  }

  private def removeCentreCmdToEvent(cmd: MembershipRemoveCentreCmd, membership: Membership)
      : ServiceValidation[MembershipEvent] = {
    val centreId = CentreId(cmd.centreId)
    if (membership.centreData.allEntities) {
      ServiceError(s"membership is for all centres, cannot remove: ${centreId}").failureNel[MembershipEvent]
    } else if (!membership.centreData.ids.exists(_ == centreId)) {
      ServiceError(s"centre ID is not in membership: ${centreId}").failureNel[MembershipEvent]
    } else {
      MembershipEvent(cmd.sessionUserId).update(
        _.time                  := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        _.id                    := membership.id.id,
        _.centreRemoved.version := cmd.expectedVersion,
        _.centreRemoved.id      := cmd.centreId
      ).successNel[String]
    }
  }

  private def removeMembershipCmdToEvent(cmd: RemoveMembershipCmd, membership: Membership)
      : ServiceValidation[MembershipEvent] = {
    MembershipEvent(cmd.sessionUserId).update(
      _.time            := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
      _.id              := membership.id.id,
      _.removed.version := cmd.expectedVersion
    ).successNel[String]
  }

  private def applyAddedEvent(event: MembershipEvent): Unit = {
    if (!event.eventType.isAdded) {
      log.error(s"applyAddedEvent: invalid event type: $event")
    } else {
      val addedEvent = event.getAdded
      val timeAdded = OffsetDateTime.parse(event.getTime)

      val v = Membership.create(id           = MembershipId(event.id),
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

  private def applyNameUpdatedEvent(event: MembershipEvent): Unit = {
    onValidMembershipEventAndVersion(event,
                                     event.eventType.isNameUpdated,
                                     event.getNameUpdated.getVersion) {
      (membership, _, time) =>
      membership.withName(event.getNameUpdated.getName).map { updated =>
        membershipRepository.put(
          updated.copy(slug         = membershipRepository.uniqueSlugFromStr(updated.name),
                       timeModified = Some(time)))
        true
      }
    }
  }

  private def applyDescriptionUpdatedEvent(event: MembershipEvent): Unit = {
    onValidMembershipEventAndVersion(event,
                                     event.eventType.isDescriptionUpdated,
                                     event.getDescriptionUpdated.getVersion) {
      (membership, _, time) =>
      membership.withDescription(event.getDescriptionUpdated.description).map { updated =>
        membershipRepository.put(updated.copy(timeModified = Some(time)))
        true
      }
    }
  }

  private def applyUserAddedEvent(event: MembershipEvent): Unit = {
    onValidMembershipEventAndVersion(event,
                                     event.eventType.isUserAdded,
                                     event.getUserAdded.getVersion) { (membership, _, time) =>

      // remove this user from other memberships
      val userIdToAdd = UserId(event.getUserAdded.getId)
      membershipRepository.getValues
        .filter { membership => membership.userIds.find(_ == userIdToAdd).isDefined }
        .foreach { membership =>
          val updated = membership.removeUser(userIdToAdd).copy(timeModified = Some(time))
          membershipRepository.put(updated)
        }

      val updated = membership.addUser(userIdToAdd).copy(timeModified = Some(time))
      membershipRepository.put(updated)
      true.successNel[String]
    }
  }

  private def applyStudyDataUpdateEvent(event: MembershipEvent): Unit = {
    onValidMembershipEventAndVersion(event,
                                     event.eventType.isStudyDataUpdated,
                                     event.getStudyDataUpdated.getVersion) {
      (membership, _, time) =>
      val subEvent = event.getStudyDataUpdated
      val studyData = MembershipEntitySet(subEvent.getAllStudies,
                                          subEvent.studyIds.map(id => StudyId(id)).toSet)
      val updated = membership.copy(version = membership.version + 1,
                                    studyData    = studyData,
                                    timeModified = Some(time))
      membershipRepository.put(updated)
      true.successNel[String]
    }
  }

  private def applyAllStudiesEvent(event: MembershipEvent): Unit = {
    onValidMembershipEventAndVersion(event,
                                     event.eventType.isAllStudies,
                                     event.getAllStudies.getVersion) {
      (membership, _, time) =>
      val updated = membership.hasAllStudies.copy(timeModified = Some(time))
      membershipRepository.put(updated)
      true.successNel[String]
    }
  }

  private def applyStudyAddedEvent(event: MembershipEvent): Unit = {
    onValidMembershipEventAndVersion(event,
                                     event.eventType.isStudyAdded,
                                     event.getStudyAdded.getVersion) {
      (membership, _, time) =>
      val updated = membership
        .addStudy(StudyId(event.getStudyAdded.getId))
        .copy(timeModified = Some(time))
      membershipRepository.put(updated)
      true.successNel[String]
    }
  }

  private def applyCentreDataUpdateEvent(event: MembershipEvent): Unit = {
    onValidMembershipEventAndVersion(event,
                                     event.eventType.isCentreDataUpdated,
                                     event.getCentreDataUpdated.getVersion) {
      (membership, _, time) =>
      val subEvent = event.getCentreDataUpdated
      val centreData = MembershipEntitySet(subEvent.getAllCentres,
                                           subEvent.centreIds.map(id => CentreId(id)).toSet)
      val updated = membership.copy(version     = membership.version + 1,
                                    centreData  = centreData,
                                    timeModified = Some(time))
      membershipRepository.put(updated)
      true.successNel[String]
    }
  }

  private def applyAllCentresEvent(event: MembershipEvent): Unit = {
    onValidMembershipEventAndVersion(event,
                                     event.eventType.isAllCentres,
                                     event.getAllCentres.getVersion) {
      (membership, _, time) =>
      val updated = membership.hasAllCentres.copy(timeModified = Some(time))
      membershipRepository.put(updated)
      true.successNel[String]
    }
  }

  private def applyCentreAddedEvent(event: MembershipEvent): Unit = {
    onValidMembershipEventAndVersion(event,
                                     event.eventType.isCentreAdded,
                                     event.getCentreAdded.getVersion) {
      (membership, _, time) =>
      val updated = membership
        .addCentre(CentreId(event.getCentreAdded.getId))
        .copy(timeModified = Some(time))
      membershipRepository.put(updated)
      true.successNel[String]
    }
  }

  private def applyUserRemovedEvent(event: MembershipEvent): Unit = {
    onValidMembershipEventAndVersion(event,
                                     event.eventType.isUserRemoved,
                                     event.getUserRemoved.getVersion) {
      (membership, _, time) =>
      val updated = membership
        .removeUser(UserId(event.getUserRemoved.getId))
        .copy(timeModified = Some(time))
      membershipRepository.put(updated)
      true.successNel[String]
    }
  }

  private def applyStudyRemovedEvent(event: MembershipEvent): Unit = {
    onValidMembershipEventAndVersion(event,
                                     event.eventType.isStudyRemoved,
                                     event.getStudyRemoved.getVersion) {
      (membership, _, time) =>
      val updated = membership
        .removeStudy(StudyId(event.getStudyRemoved.getId))
        .copy(timeModified = Some(time))
      membershipRepository.put(updated)
      true.successNel[String]
    }
  }

  private def applyCentreRemovedEvent(event: MembershipEvent): Unit = {
    onValidMembershipEventAndVersion(event,
                                     event.eventType.isCentreRemoved,
                                     event.getCentreRemoved.getVersion) {
      (membership, _, time) =>
      val updated = membership
        .removeCentre(CentreId(event.getCentreRemoved.getId))
        .copy(timeModified = Some(time))
      membershipRepository.put(updated)
      true.successNel[String]
    }
  }

  private def applyMembershipRemovedEvent(event: MembershipEvent): Unit = {
    onValidMembershipEventAndVersion(event,
                                     event.eventType.isRemoved,
                                     event.getRemoved.getVersion) {
      (membership, _, time) =>
      membershipRepository.remove(membership)
      true.successNel[String]
    }
  }

  private def processUpdateMembershipCmd[T <: MembershipModifyCommand]
    (cmd: T,
     cmdToEvent: (T, Membership) => ServiceValidation[MembershipEvent],
     applyEvent: MembershipEvent => Unit): Unit = {
    val event = for {
        membership   <- membershipRepository.getByKey(MembershipId(cmd.membershipId))
        validVersion <- membership.requireVersion(cmd.expectedVersion)
        event        <- cmdToEvent(cmd, membership)
      } yield event
    process(event)(applyEvent)
  }

  private def onValidMembershipEventAndVersion(event:        MembershipEvent,
                                               eventType:    Boolean,
                                               eventVersion: Long)
                                              (applyEvent:  (Membership,
                                                             MembershipEvent,
                                                             OffsetDateTime) => ServiceValidation[Boolean])
      : Unit = {
    if (!eventType) {
      log.error(s"invalid event type: $event")
    } else {
      membershipRepository.getByKey(MembershipId(event.id)).fold(
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
    membershipRepository.init
  }

  init
}
