package org.biobank.services.access

import akka.actor._
import akka.persistence.{RecoveryCompleted, SnapshotOffer, SaveSnapshotSuccess, SaveSnapshotFailure}
//import com.github.ghik.silencer.silent
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import org.biobank.domain.access._
import org.biobank.domain.users.UserId
import org.biobank.infrastructure.commands.AccessCommands._
import org.biobank.infrastructure.events.AccessEvents._
import org.biobank.services.{Processor, ServiceValidation, SnapshotWriter}
import play.api.libs.json._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._
import scalaz._

object AccessProcessor {

  def props: Props = Props[AccessProcessor]

  final case class SnapshotState(accessItems: Set[AccessItem])

  implicit val snapshotStateFormat: Format[SnapshotState] = Json.format[SnapshotState]

}

/**
 * Handles commands related to access.
 */
class AccessProcessor @Inject() (val accessItemRepository: AccessItemRepository,
                                 val snapshotWriter:       SnapshotWriter)
    extends Processor {

  import AccessProcessor._
  import org.biobank.CommonValidations._

  type ApplyRoleEvent = (Role, OffsetDateTime) => ServiceValidation[Boolean]

  override def persistenceId: String = "access-processor-id"

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var replyTo: Option[ActorRef] = None

  @SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.Throw"))
  val receiveRecover: Receive = {

    case event: AccessEvent =>
      event.eventType match {
        case _: AccessEvent.EventType.Role =>
          event.getRole.eventType match {
            case _: AccessEvent.Role.EventType.Added              => applyRoleAddedEvent(event)
            case _: AccessEvent.Role.EventType.NameUpdated        => applyNameUpdatedEvent(event)
            case _: AccessEvent.Role.EventType.DescriptionUpdated => applyDescriptionUpdatedEvent(event)
            case _: AccessEvent.Role.EventType.UserAdded          => applyUserAddedEvent(event)
            case _: AccessEvent.Role.EventType.ParentAdded        => applyParentAddedEvent(event)
            case _: AccessEvent.Role.EventType.ChildAdded         => applyChildAddedEvent(event)
            case _: AccessEvent.Role.EventType.UserRemoved        => applyUserRemovedEvent(event)
            case _: AccessEvent.Role.EventType.ParentRemoved      => applyParentRemovedEvent(event)
            case _: AccessEvent.Role.EventType.ChildRemoved       => applyChildRemovedEvent(event)
            case _: AccessEvent.Role.EventType.Removed            => applyRoleRemovedEvent(event)
            case _ => throw new Exception(s"role event not handled: $event")
          }

        case _ => throw new Exception(s"access event not handled: $event")
      }

    case SnapshotOffer(_, snapshotFilename: String) =>
      applySnapshot(snapshotFilename)

    case RecoveryCompleted =>
      log.debug(s"AccessProcessor: recovery completed")

    case event => throw new Exception(s"event not handled: $event")
  }

  @SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.Throw"))
  val receiveCommand: Receive = {
    case accessCommand: AccessCommand =>
      accessCommand match {
        // case cmd: AddUserToRoleCmd =>
        //   processUpdateRoleCmd(cmd, addUserToRoleCmdToEvent, applyUserAddedToRoleEvent)
        case cmd: AddRoleCmd =>
          process(addRoleCmdToEvent(cmd))(applyRoleAddedEvent)
        case cmd: RoleUpdateNameCmd =>
          processUpdateRoleCmd(cmd, updateNameCmdToEvent, applyNameUpdatedEvent)
        case cmd: RoleUpdateDescriptionCmd =>
          processUpdateRoleCmd(cmd, updateDescriptionCmdToEvent, applyDescriptionUpdatedEvent)
        case cmd: RoleAddUserCmd =>
          processUpdateRoleCmd(cmd, addUserCmdToEvent, applyUserAddedEvent)
        case cmd: RoleAddParentCmd =>
          processUpdateRoleCmd(cmd, addParentCmdToEvent, applyParentAddedEvent)
        case cmd: RoleAddChildCmd =>
          processUpdateRoleCmd(cmd, addChildCmdToEvent, applyChildAddedEvent)
        case cmd: RoleRemoveUserCmd =>
          processUpdateRoleCmd(cmd, removeUserCmdToEvent, applyUserRemovedEvent)
        case cmd: RoleRemoveParentCmd =>
          processUpdateRoleCmd(cmd, removeParentCmdToEvent, applyParentRemovedEvent)
        case cmd: RoleRemoveChildCmd =>
          processUpdateRoleCmd(cmd, removeChildCmdToEvent, applyChildRemovedEvent)
       case cmd: RemoveRoleCmd =>
          processUpdateRoleCmd(cmd, removeRoleCmdToEvent, applyRoleRemovedEvent)
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

    case cmd => log.error(s"accessProcessor: message not handled: $cmd")
  }

  private def mySaveSnapshot(): Unit = {
    val snapshotState = SnapshotState(accessItemRepository.getValues.toSet)
    val filename = snapshotWriter.save(persistenceId, Json.toJson(snapshotState).toString)
    saveSnapshot(filename)
  }

  private def applySnapshot(filename: String): Unit = {
    log.debug(s"snapshot recovery file: $filename")
    val fileContents = snapshotWriter.load(filename);
    Json.parse(fileContents).validate[SnapshotState].fold(
      errors => log.error(s"could not apply snapshot: $filename: $errors"),
      snapshot =>  {
        log.debug(s"snapshot contains ${snapshot.accessItems.size} accessItems")
        snapshot.accessItems.foreach(accessItemRepository.put)
      }
    )
  }

  private def addRoleCmdToEvent(cmd: AddRoleCmd): ServiceValidation[AccessEvent] = {
    for {
      name    <- nameAvailable(cmd.name)
      roleId  <- validNewIdentity(accessItemRepository.nextIdentity, accessItemRepository)
      newRole <- Role.create(id           = roleId,
                             version      = 0L,
                             timeAdded    = OffsetDateTime.now,
                             timeModified = None,
                             name         = cmd.name,
                             description  = cmd.description,
                             userIds      = cmd.userIds.map(id => UserId(id)).toSet,
                             parentIds    = cmd.parentIds.map(id => AccessItemId(id)).toSet,
                             childrenIds  = cmd.childrenIds.map(id => AccessItemId(id)).toSet)
    } yield AccessEvent(cmd.sessionUserId).update(
      _.time                           := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
      _.role.id                        := roleId.id,
      _.role.added.name                := cmd.name,
      _.role.added.optionalDescription := cmd.description,
      _.role.added.userIds             := cmd.userIds,
      _.role.added.parentIds           := cmd.parentIds,
      _.role.added.childrenIds         := cmd.childrenIds);

  }

  private def updateNameCmdToEvent(cmd: RoleUpdateNameCmd, role: Role): ServiceValidation[AccessEvent] = {
    (nameAvailable(cmd.name, AccessItemId(cmd.roleId)) |@|
       role.withName(cmd.name)) { case _ =>
        AccessEvent(cmd.sessionUserId).update(
          _.time                     := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
          _.role.id                  := role.id.id,
          _.role.nameUpdated.version := cmd.expectedVersion,
          _.role.nameUpdated.name    := cmd.name)
    }
  }

  private def updateDescriptionCmdToEvent(cmd: RoleUpdateDescriptionCmd, role: Role)
      : ServiceValidation[AccessEvent] = {
    role.withDescription(cmd.description).map { _ =>
      val timeStr = OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
      AccessEvent(cmd.sessionUserId).update(
        _.time
          := timeStr,
        _.role.id                                     := role.id.id,
        _.role.descriptionUpdated.version             := cmd.expectedVersion,
        _.role.descriptionUpdated.optionalDescription := cmd.description)
    }
  }

  private def addUserCmdToEvent(cmd: RoleAddUserCmd, role: Role): ServiceValidation[AccessEvent] = {
    val userId = UserId(cmd.userId)
    if (role.userIds.exists(_ == userId)) {
      EntityCriteriaError(s"user ID is already in role: ${userId}").failureNel[AccessEvent]
    } else {
      AccessEvent(cmd.sessionUserId).update(
        _.time                   := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        _.role.id                := role.id.id,
        _.role.userAdded.version := cmd.expectedVersion,
        _.role.userAdded.id      := cmd.userId
      ).successNel[String]
    }
  }

  private def addParentCmdToEvent(cmd: RoleAddParentCmd, role: Role): ServiceValidation[AccessEvent] = {
    role.addParent(AccessItemId(cmd.parentRoleId)).map { updated =>
      AccessEvent(cmd.sessionUserId).update(
        _.time                     := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        _.role.id                  := role.id.id,
        _.role.parentAdded.version := cmd.expectedVersion,
        _.role.parentAdded.id      := cmd.parentRoleId
      )
    }
  }

  private def addChildCmdToEvent(cmd: RoleAddChildCmd, role: Role): ServiceValidation[AccessEvent] = {
    role.addChild(AccessItemId(cmd.childRoleId)).map { updated =>
      AccessEvent(cmd.sessionUserId).update(
        _.time                    := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        _.role.id                 := role.id.id,
        _.role.childAdded.version := cmd.expectedVersion,
        _.role.childAdded.id      := cmd.childRoleId
      )
    }
  }

  private def removeUserCmdToEvent(cmd: RoleRemoveUserCmd, role: Role): ServiceValidation[AccessEvent] = {
    val userId = UserId(cmd.userId)
    if (!role.userIds.exists(_ == userId)) {
      EntityCriteriaError(s"user ID is not in role: ${userId}").failureNel[AccessEvent]
    } else {
      AccessEvent(cmd.sessionUserId).update(
        _.time                     := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        _.role.id                  := role.id.id,
        _.role.userRemoved.version := cmd.expectedVersion,
        _.role.userRemoved.id      := cmd.userId
      ).successNel[String]
    }
  }

  private def removeParentCmdToEvent(cmd:  RoleRemoveParentCmd,
                                     role: Role): ServiceValidation[AccessEvent] = {
    role.removeParent(AccessItemId(cmd.parentRoleId)).map { updated =>
      AccessEvent(cmd.sessionUserId).update(
        _.time                       := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        _.role.id                    := role.id.id,
        _.role.parentRemoved.version := cmd.expectedVersion,
        _.role.parentRemoved.id      := cmd.parentRoleId
      )
    }
  }

  private def removeChildCmdToEvent(cmd:  RoleRemoveChildCmd,
                                    role: Role): ServiceValidation[AccessEvent] = {
    role.removeChild(AccessItemId(cmd.childRoleId)).map { udpated =>
      AccessEvent(cmd.sessionUserId).update(
        _.time                       := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        _.role.id                    := role.id.id,
        _.role.childRemoved.version := cmd.expectedVersion,
        _.role.childRemoved.id      := cmd.childRoleId
      )
    }
  }

  private def removeRoleCmdToEvent(cmd: RemoveRoleCmd, role: Role): ServiceValidation[AccessEvent] = {
    AccessEvent(cmd.sessionUserId).update(
      _.time                 := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
      _.role.id              := role.id.id,
      _.role.removed.version := cmd.expectedVersion
    ).successNel[String]
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

  private def applyRoleAddedEvent(event: AccessEvent): Unit = {
    if (!event.eventType.isRole || !event.getRole.eventType.isAdded) {
      log.error(s"applyAddedEvent: invalid event type: $event")
    } else {
      val addedEvent = event.getRole.getAdded
      val timeAdded = OffsetDateTime.parse(event.getTime)

      val v = Role.create(id           = AccessItemId(event.getRole.getId),
                          version      = 0L,
                          timeAdded    = timeAdded,
                          timeModified = None,
                          name         = addedEvent.getName,
                          description  = addedEvent.description,
                          userIds      = addedEvent.userIds.map(id => UserId(id)).toSet,
                          parentIds    = addedEvent.parentIds.map(id => AccessItemId(id)).toSet,
                          childrenIds  = addedEvent.childrenIds.map(id => AccessItemId(id)).toSet)

      if (v.isFailure) {
        log.error(s"could not add role from event: $v")
      }

      v.foreach(accessItemRepository.put)
    }
  }

  private def onValidRoleEventAndVersion(event:        AccessEvent,
                                         eventType:    Boolean,
                                         eventVersion: Long)
                                        (applyEvent: ApplyRoleEvent): Unit = {
    if (!eventType) {
      log.error(s"invalid role event type: $event")
    } else {
      accessItemRepository.getRole(AccessItemId(event.getRole.getId)).fold(
        err => log.error(s"role from event does not exist: $err"),
        role => {
          if (role.version != eventVersion) {
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


  private def updateRole(role: Role, time: OffsetDateTime): Boolean = {
    accessItemRepository.put(role.copy(timeModified = Some(time)))
    true
  }

  private def applyNameUpdatedEvent(event: AccessEvent): Unit = {
    onValidRoleEventAndVersion(event,
                               event.getRole.eventType.isNameUpdated,
                               event.getRole.getNameUpdated.getVersion) {
      (role, time) =>
      role.withName(event.getRole.getNameUpdated.getName).map { r =>
        accessItemRepository.put(r.copy(slug         = accessItemRepository.slug(r.name),
                                        timeModified = Some(time)))
        true
      }
    }
  }

  private def applyDescriptionUpdatedEvent(event: AccessEvent): Unit = {
    onValidRoleEventAndVersion(event,
                               event.getRole.eventType.isDescriptionUpdated,
                               event.getRole.getDescriptionUpdated.getVersion) {
      (role, time) =>
      role.withDescription(event.getRole.getDescriptionUpdated.description).map(r => updateRole(r, time))
    }
  }

  private def applyUserAddedEvent(event: AccessEvent): Unit = {
    onValidRoleEventAndVersion(event,
                               event.getRole.eventType.isUserAdded,
                               event.getRole.getUserAdded.getVersion) { (role, time) =>
      role.addUser(UserId(event.getRole.getUserAdded.getId)).map(r => updateRole(r, time))
    }
  }

  private def applyParentAddedEvent(event: AccessEvent): Unit = {
    onValidRoleEventAndVersion(event,
                               event.getRole.eventType.isParentAdded,
                               event.getRole.getParentAdded.getVersion) { (role, time) =>
      role.addParent(AccessItemId(event.getRole.getParentAdded.getId)).map(r => updateRole(r, time))
    }
  }

  private def applyChildAddedEvent(event: AccessEvent): Unit = {
    onValidRoleEventAndVersion(event,
                               event.getRole.eventType.isChildAdded,
                               event.getRole.getChildAdded.getVersion) { (role, time) =>
      role.addChild(AccessItemId(event.getRole.getChildAdded.getId)).map(r => updateRole(r, time))
    }
  }

  private def applyUserRemovedEvent(event: AccessEvent): Unit = {
    onValidRoleEventAndVersion(event,
                               event.getRole.eventType.isUserRemoved,
                               event.getRole.getUserRemoved.getVersion) { (role, time) =>
      role.removeUser(UserId(event.getRole.getUserRemoved.getId)).map(r => updateRole(r, time))
    }
  }

  private def applyParentRemovedEvent(event: AccessEvent): Unit = {
    onValidRoleEventAndVersion(event,
                               event.getRole.eventType.isParentRemoved,
                               event.getRole.getParentRemoved.getVersion) {
      (role, time) =>
      role.removeParent(AccessItemId(event.getRole.getParentRemoved.getId)).map(r => updateRole(r, time))
    }
  }

  private def applyChildRemovedEvent(event: AccessEvent): Unit = {
    onValidRoleEventAndVersion(event,
                               event.getRole.eventType.isChildRemoved,
                               event.getRole.getChildRemoved.getVersion) {
      (role, time) =>
      role.removeChild(AccessItemId(event.getRole.getChildRemoved.getId)).map(r => updateRole(r, time))
    }
  }

  private def applyRoleRemovedEvent(event: AccessEvent): Unit = {
    onValidRoleEventAndVersion(event,
                               event.getRole.eventType.isRemoved,
                               event.getRole.getRemoved.getVersion) {
      (role, time) =>
      accessItemRepository.remove(role)
      true.successNel[String]
    }
  }

  val ErrMsgNameExists: String = "name already used"

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  private def nameAvailable(name: String): ServiceValidation[Boolean] = {
    nameAvailableMatcher(name, accessItemRepository, ErrMsgNameExists) { item =>
      item.name == name
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  private def nameAvailable(name: String, excludeId: AccessItemId): ServiceValidation[Boolean] = {
    nameAvailableMatcher(name, accessItemRepository, ErrMsgNameExists){ item =>
      (item.name == name) && (item.id != excludeId)
    }
  }

  private def init(): Unit = {
    accessItemRepository.init
  }

  init
}
