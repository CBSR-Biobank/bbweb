package org.biobank.service.access

import akka.actor._
import akka.event.{Logging, LoggingAdapter}
import akka.persistence.{RecoveryCompleted, SnapshotOffer, SaveSnapshotSuccess, SaveSnapshotFailure}
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import org.biobank.domain.access._
import org.biobank.domain.user.UserId
import org.biobank.infrastructure.command.AccessCommands._
import org.biobank.infrastructure.event.AccessEvents._
//import org.biobank.infrastructure.event.EventUtils
import org.biobank.service.{Processor, ServiceValidation, SnapshotWriter}
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
            case _: AccessEvent.Role.EventType.UserAdded => applyUserAddedToRoleEvent(event)
            case _ => log.error(s"role event not handled: $event")
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
      }

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
        log.info(s"snapshot contains ${snapshot.accessItems.size} access")
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
  private def addUserToRoleCmdToEvent(cmd: AddUserToRoleCmd, role: Role): ServiceValidation[AccessEvent] = {
    accessItemRepository.getRole(AccessItemId(cmd.roleId)).map { role =>
      AccessEvent(cmd.sessionUserId).update(
        _.time                   := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        _.role.roleId            := cmd.roleId,
        _.role.version           := cmd.expectedVersion,
        _.role.userAdded.userId  := cmd.userId
      )
    }
  }

  private def onValidRoleEvent(event: AccessEvent)
                              (applyEvent: (Role, OffsetDateTime) => ServiceValidation[Boolean])
      : Unit = {
    if (!event.eventType.isRole) {
      log.error(s"invalid role event type: $event")
    } else {
      accessItemRepository.getRole(AccessItemId(event.getRole.getRoleId)).fold(
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
      accessItemRepository.put(role.addUser(UserId(event.getRole.getUserAdded.getUserId)))
      true.successNel[String]
    }
  }


}
