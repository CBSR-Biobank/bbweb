package org.biobank.service.study

import org.biobank.service.{ Processor, WrappedCommand, WrappedEvent }
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.domain.{
  DomainValidation,
  DomainError
}
import org.biobank.domain. {
  AnatomicalSourceType,
  PreservationType,
  PreservationTemperatureType,
  SpecimenType
}
import org.biobank.domain.user.UserId
import org.biobank.domain.study.{
  CollectionEventTypeRepository,
  SpecimenGroup,
  SpecimenGroupId,
  SpecimenGroupRepository,
  SpecimenLinkTypeRepository,
  Study,
  StudyId
}

import akka.persistence.SnapshotOffer
import scaldi.akka.AkkaInjectable
import scaldi.{Injectable, Injector}
import org.joda.time.DateTime
import scalaz._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

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
        case event: SpecimenGroupAddedEvent   => recoverSpecimenGroupAddedEvent  (event, wevent.userId, wevent.dateTime)
        case event: SpecimenGroupUpdatedEvent => recoverSpecimenGroupUpdatedEvent(event, wevent.userId, wevent.dateTime)
        case event: SpecimenGroupRemovedEvent => recoverSpecimenGroupRemovedEvent(event, wevent.userId, wevent.dateTime)

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
        case cmd: AddSpecimenGroupCmd    => processAddSpecimenGroupCmd(cmd)
        case cmd: UpdateSpecimenGroupCmd => processUpdateSpecimenGroupCmd(cmd)
        case cmd: RemoveSpecimenGroupCmd => processRemoveSpecimenGroupCmd(cmd)
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

  private def processAddSpecimenGroupCmd
    (cmd: AddSpecimenGroupCmd)
    (implicit userId: Option[UserId])
      : Unit = {
    val timeNow = DateTime.now
    val sgId = specimenGroupRepository.nextIdentity

    if (specimenGroupRepository.getByKey(sgId).isSuccess) {
      throw new IllegalStateException(s"specimen group with id already exsits: $id")
    }

    val event = for {
      nameValid <- nameAvailable(cmd.name)
      newItem <-SpecimenGroup.create(
        StudyId(cmd.studyId), sgId, -1, timeNow, cmd.name, cmd.description,
        cmd.units, cmd.anatomicalSourceType, cmd.preservationType,
        cmd.preservationTemperatureType, cmd.specimenType)
      newEvent <- SpecimenGroupAddedEvent(
        studyId                     = newItem.studyId.id,
        specimenGroupId             = newItem.id.id,
        name                        = Some(newItem.name),
        description                 = newItem.description,
        units                       = Some(newItem.units),
        anatomicalSourceType        = Some(newItem.anatomicalSourceType.toString),
        preservationType            = Some(newItem.preservationType.toString),
        preservationTemperatureType = Some(newItem.preservationTemperatureType.toString),
        specimenType                = Some(newItem.specimenType.toString)).success
    } yield newEvent

    process(event){ wevent =>
      recoverSpecimenGroupAddedEvent(wevent.event, wevent.userId, wevent.dateTime)
    }
  }

  private def processUpdateSpecimenGroupCmd
    (cmd: UpdateSpecimenGroupCmd)
    (implicit userId: Option[UserId])
      : Unit = {
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

    val event = v.fold(
      err => err.failure[SpecimenGroupUpdatedEvent],
      sg => SpecimenGroupUpdatedEvent(
        studyId                     = cmd.studyId,
        specimenGroupId             = sg.id.id,
        version                     = Some(sg.version),
        name                        = Some(cmd.name),
        description                 = cmd.description,
        units                       = Some(cmd.units),
        anatomicalSourceType        = Some(cmd.anatomicalSourceType.toString),
        preservationType            = Some(cmd.preservationType.toString),
        preservationTemperatureType = Some(cmd.preservationTemperatureType.toString),
        specimenType                = Some(cmd.specimenType.toString)).success
    )

    process(event){ wevent =>
      recoverSpecimenGroupUpdatedEvent(wevent.event, wevent.userId, wevent.dateTime)
    }
  }

  private def processRemoveSpecimenGroupCmd
    (cmd: RemoveSpecimenGroupCmd)
    (implicit userId: Option[UserId])
      : Unit = {
    val v = update(cmd) { sg => sg.success }

    val event = v.fold(
      err => err.failure[SpecimenGroupRemovedEvent],
      sg =>  SpecimenGroupRemovedEvent(sg.studyId.id, sg.id.id).success
    )

    process(event){ wevent =>
      recoverSpecimenGroupRemovedEvent(wevent.event, wevent.userId, wevent.dateTime)
    }
  }

  private def recoverSpecimenGroupAddedEvent
    (event: SpecimenGroupAddedEvent, userId: Option[UserId], dateTime: DateTime)
      : Unit = {
    specimenGroupRepository.put(SpecimenGroup(
      studyId                     = StudyId(event.studyId),
      id                          = SpecimenGroupId(event.specimenGroupId),
      version                     = 0L,
      timeAdded                   = dateTime,
      timeModified                = None,
      name                        = event.getName,
      description                 = event.description,
      units                       = event.getUnits,
      anatomicalSourceType        = AnatomicalSourceType.withName(event.getAnatomicalSourceType),
      preservationType            = PreservationType.withName(event.getPreservationType),
      preservationTemperatureType = PreservationTemperatureType.withName(event.getPreservationTemperatureType),
      specimenType                = SpecimenType.withName(event.getSpecimenType)
    ))
    ()
  }

  private def recoverSpecimenGroupUpdatedEvent
    (event: SpecimenGroupUpdatedEvent, userId: Option[UserId], dateTime: DateTime)
      : Unit = {
    specimenGroupRepository.getByKey(SpecimenGroupId(event.specimenGroupId)).fold(
      err => throw new IllegalStateException(s"updating annotation type from event failed: $err"),
      sg => specimenGroupRepository.put(sg.copy(
        version                     = event.getVersion,
        timeModified                = Some(dateTime),
        name                        = event.getName,
        description                 = event.description,
        units                       = event.getUnits,
        anatomicalSourceType        = AnatomicalSourceType.withName(event.getAnatomicalSourceType),
        preservationType            = PreservationType.withName(event.getPreservationType),
        preservationTemperatureType = PreservationTemperatureType.withName(event.getPreservationTemperatureType),
        specimenType                = SpecimenType.withName(event.getSpecimenType)
      ))
    )
    ()
  }

  private def recoverSpecimenGroupRemovedEvent
    (event: SpecimenGroupRemovedEvent, userId: Option[UserId], dateTime: DateTime)
      : Unit = {
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
      if (collectionEventTypeRepository.specimenGroupCanBeUpdated(studyId, specimenGroupId)) {
        DomainError(s"specimen group is in use by collection event type: $specimenGroupId").failureNel
      } else {
        true.success
      }
    }

    def checkNotInUseBySpecimenLinkType: DomainValidation[Boolean] = {
      if (specimenLinkTypeRepository.specimenGroupCanBeUpdated(specimenGroupId)) {
        DomainError(s"specimen group is in use by specimen link type: $specimenGroupId").failureNel
      } else {
        true.success
      }
    }

    (checkNotInUseByCollectionEventType |@| checkNotInUseBySpecimenLinkType) { case (_, _) => true }
  }

}
