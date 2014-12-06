package org.biobank.service.study

import org.biobank.service.{ Processor, WrappedCommand, WrappedEvent }
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.domain.{
  DomainValidation,
  DomainError
}
import org.biobank.domain.user.UserId
import org.biobank.domain.AnatomicalSourceType._
import org.biobank.domain.PreservationType._
import org.biobank.domain.PreservationTemperatureType._
import org.biobank.domain.SpecimenType._
import org.biobank.domain.study.{
  CollectionEventTypeRepository,
  SpecimenGroup,
  SpecimenGroupId,
  SpecimenGroupRepository,
  SpecimenLinkTypeRepository,
  Study,
  StudyId }

import akka.persistence.SnapshotOffer
import scaldi.akka.AkkaInjectable
import scaldi.{Injectable, Injector}
import org.joda.time.DateTime
import scalaz._
import scalaz.Scalaz._

/** The SpecimenGroupProcessor is responsible for maintaining state changes for all
  * [[org.biobank.domain.study.SpecimenGroup]] aggregates. This particular processor uses Akka-Persistence's
  * [[akka.persistence.PersistentActor]]. It receives Commands and if valid will persist the generated
  * events, afterwhich it will updated the current state of the [[org.biobank.domain.study.SpecimenGroup]]
  * being processed.
  *
  * It is a child actor of  [[org.biobank.service.study.StudiesProcessorComponent.StudiesProcessor]].
  *
  * It handles commands that deal with a Specimen Group.
  *
  */
class SpecimenGroupProcessor(implicit inj: Injector) extends Processor with AkkaInjectable {

  override def persistenceId = "specimen-group-processor-id"

  case class SnapshotState(specimenGroups: Set[SpecimenGroup])

  val collectionEventTypeRepository = inject [CollectionEventTypeRepository]

  val specimenGroupRepository = inject [SpecimenGroupRepository]

  val specimenLinkTypeRepository = inject [SpecimenLinkTypeRepository]

  /**
    * These are the events that are recovered during journal recovery. They cannot fail and must be
    * processed to recreate the current state of the aggregate.
    */
  val receiveRecover: Receive = {
    case wevent: WrappedEvent[_] =>
      wevent.event match {
        case event: SpecimenGroupAddedEvent =>   recoverEvent(event, wevent.userId, wevent.dateTime)
        case event: SpecimenGroupUpdatedEvent => recoverEvent(event, wevent.userId, wevent.dateTime)
        case event: SpecimenGroupRemovedEvent => recoverEvent(event, wevent.userId, wevent.dateTime)

        case event => throw new IllegalStateException(s"event not handled: $event")
      }

    case SnapshotOffer(_, snapshot: SnapshotState) =>
      snapshot.specimenGroups.foreach{ specimenGroup => specimenGroupRepository.put(specimenGroup) }
  }

  /**
    * These are the commands that are requested. A command can fail, and will send the failure as a response
    * back to the user. Each valid command generates one or more events and is journaled.
    */
  val receiveCommand: Receive = {

    case procCmd: WrappedCommand =>
      implicit val userId = procCmd.userId
      procCmd.command match {
        case cmd: AddSpecimenGroupCmd =>    process(validateCmd(cmd)){ wevent => recoverEvent(wevent.event, wevent.userId, wevent.dateTime) }
        case cmd: UpdateSpecimenGroupCmd => process(validateCmd(cmd)){ wevent => recoverEvent(wevent.event, wevent.userId, wevent.dateTime) }
        case cmd: RemoveSpecimenGroupCmd => process(validateCmd(cmd)){ wevent => recoverEvent(wevent.event, wevent.userId, wevent.dateTime) }
      }

    case "snap" =>
      saveSnapshot(SnapshotState(specimenGroupRepository.getValues.toSet))
      stash()

    case cmd => log.error(s"SpecimenGroupProcessor: message not handled: $cmd")

  }

  def update
    (cmd: SpecimenGroupModifyCommand)
    (fn: SpecimenGroup => DomainValidation[SpecimenGroup])
      : DomainValidation[SpecimenGroup] = {
    for {
      sg <- specimenGroupRepository.withId(StudyId(cmd.studyId), SpecimenGroupId(cmd.id))
      notInUse <- checkNotInUse(sg.studyId, sg.id)
      validVersion <- sg.requireVersion(cmd.expectedVersion)
      updatedSg <- fn(sg)
    } yield updatedSg
  }

  private def validateCmd(cmd: AddSpecimenGroupCmd): DomainValidation[SpecimenGroupAddedEvent] = {
    val timeNow = DateTime.now
    val sgId = specimenGroupRepository.nextIdentity

    if (specimenGroupRepository.getByKey(sgId).isSuccess) {
      throw new IllegalStateException(s"specimen group with id already exsits: $id")
    }

    for {
      nameValid <- nameAvailable(cmd.name)
      newItem <-SpecimenGroup.create(
        StudyId(cmd.studyId), sgId, -1, timeNow, cmd.name, cmd.description,
        cmd.units, cmd.anatomicalSourceType, cmd.preservationType,
        cmd.preservationTemperatureType, cmd.specimenType)
      newEvent <- SpecimenGroupAddedEvent(
        newItem.studyId.id, newItem.id.id, newItem.name, newItem.description,
        newItem.units, newItem.anatomicalSourceType, newItem.preservationType,
        newItem.preservationTemperatureType, newItem.specimenType).success
    } yield newEvent
  }

  private def validateCmd(
    cmd: UpdateSpecimenGroupCmd): DomainValidation[SpecimenGroupUpdatedEvent] = {
    val timeNow = DateTime.now
    val studyId = StudyId(cmd.studyId)
    val specimenGroupId = SpecimenGroupId(cmd.id)

    val v = update(cmd) { sg =>
      for {
        nameAvailable <- nameAvailable(cmd.name, specimenGroupId)
        updatedSg <- sg.update(
          cmd.name, cmd.description, cmd.units, cmd.anatomicalSourceType, cmd.preservationType,
          cmd.preservationTemperatureType, cmd.specimenType)
      } yield updatedSg
    }
    v.fold(
      err => err.fail[SpecimenGroupUpdatedEvent],
      sg => SpecimenGroupUpdatedEvent(
        cmd.studyId, sg.id.id, sg.version, cmd.name, cmd.description,
        cmd.units, cmd.anatomicalSourceType, cmd.preservationType, cmd.preservationTemperatureType,
        cmd.specimenType).success
    )
  }

  private def validateCmd(
    cmd: RemoveSpecimenGroupCmd): DomainValidation[SpecimenGroupRemovedEvent] = {
    val v = update(cmd) { sg => sg.success }

    v.fold(
      err => err.fail[SpecimenGroupRemovedEvent],
      sg =>  SpecimenGroupRemovedEvent(sg.studyId.id, sg.id.id).success
    )
  }

  private def recoverEvent(event: SpecimenGroupAddedEvent, userId: Option[UserId], dateTime: DateTime): Unit = {
    specimenGroupRepository.put(SpecimenGroup(
      StudyId(event.studyId), SpecimenGroupId(event.specimenGroupId), 0L, dateTime, None,
      event.name, event.description, event.units, event.anatomicalSourceType, event.preservationType,
      event.preservationTemperatureType, event.specimenType
    ))
    ()
  }

  private def recoverEvent(event: SpecimenGroupUpdatedEvent, userId: Option[UserId], dateTime: DateTime): Unit = {
    specimenGroupRepository.getByKey(SpecimenGroupId(event.specimenGroupId)).fold(
      err => throw new IllegalStateException(s"updating annotation type from event failed: $err"),
      sg => specimenGroupRepository.put(sg.copy(
        version                     = event.version,
        timeModified              = Some(dateTime),
        name                        = event.name,
        description                 = event.description,
        units                       = event.units,
        anatomicalSourceType        = event.anatomicalSourceType,
        preservationType            = event.preservationType,
        preservationTemperatureType = event.preservationTemperatureType,
        specimenType                = event.specimenType))
    )
    ()
  }

  private def recoverEvent(event: SpecimenGroupRemovedEvent, userId: Option[UserId], dateTime: DateTime): Unit = {
    specimenGroupRepository.getByKey(SpecimenGroupId(event.specimenGroupId)).fold(
      err => throw new IllegalStateException(s"updating annotation type from event failed: $err"),
      sg => specimenGroupRepository.remove(sg)
    )
    ()
  }

  val ErrMsgNameExists = "specimen group with name already exists"

  private def nameAvailable(specimenGroupName: String): DomainValidation[Boolean] = {
    nameAvailableMatcher(specimenGroupName, specimenGroupRepository, ErrMsgNameExists) { item =>
      item.name == specimenGroupName
    }
  }

  private def nameAvailable(
    specimenGroupName: String,
    id: SpecimenGroupId): DomainValidation[Boolean] = {
    nameAvailableMatcher(specimenGroupName, specimenGroupRepository, ErrMsgNameExists) { item =>
      (item.name == specimenGroupName) && (item.id != id)
    }
  }

  private def checkNotInUse(
    studyId: StudyId,
    specimenGroupId: SpecimenGroupId): DomainValidation[Boolean] = {

    def checkNotInUseByCollectionEventType: DomainValidation[Boolean] = {
      if (collectionEventTypeRepository.specimenGroupInUse(studyId, specimenGroupId)) {
        DomainError(s"specimen group is in use by collection event type: $specimenGroupId").failNel
      } else {
        true.success
      }
    }

    def checkNotInUseBySpecimenLinkType: DomainValidation[Boolean] = {
      if (specimenLinkTypeRepository.specimenGroupInUse(specimenGroupId)) {
        DomainError(s"specimen group is in use by specimen link type: $specimenGroupId").failNel
      } else {
        true.success
      }
    }

    (checkNotInUseByCollectionEventType |@| checkNotInUseBySpecimenLinkType) { case (_, _) => true }
  }

}
