package org.biobank.service.study

import org.biobank.domain._
import org.biobank.domain.user.UserId
import org.biobank.domain.study.{
  ProcessingType,
  ProcessingTypeId,
  ProcessingTypeRepository,
  SpecimenLinkType,
  SpecimenLinkTypeId,
  SpecimenLinkTypeRepository,
  SpecimenLinkAnnotationTypeRepository,
  SpecimenGroup,
  SpecimenGroupId,
  SpecimenGroupRepository
}
import org.slf4j.LoggerFactory
import org.biobank.service.{ Processor, WrappedEvent }
import org.biobank.infrastructure._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEventsUtil._
import org.biobank.infrastructure.event.StudyEvents._

import akka.persistence.SnapshotOffer
import org.joda.time.DateTime
import scaldi.akka.AkkaInjectable
import scaldi.{Injectable, Injector}
import scalaz._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

/**
  * The SpecimenLinkTypeProcessor is responsible for maintaining state changes for all
  * [[org.biobank.domain.study.SpecimenLinkType]] aggregates. This particular processor uses
  * Akka-Persistence's [[akka.persistence.PersistentActor]]. It receives Commands and if valid will persist
  * the generated events, afterwhich it will updated the current state of the
  * [[org.biobank.domain.study.SpecimenLinkType]] being processed.
  *
  * It is a child actor of [[org.biobank.service.study.StudiesProcessorComponent.StudiesProcessor]].
  */
class SpecimenLinkTypeProcessor(implicit inj: Injector) extends Processor with AkkaInjectable {

  override def persistenceId = "specimen-link-type-processor-id"

  case class SnapshotState(specimenLinkTypes: Set[SpecimenLinkType])

  val specimenGroupRepository = inject [SpecimenGroupRepository]

  val processingTypeRepository = inject [ProcessingTypeRepository]

  val specimenLinkTypeRepository = inject [SpecimenLinkTypeRepository]

  val specimenLinkAnnotationTypeRepository = inject [SpecimenLinkAnnotationTypeRepository]

  /**
    * These are the events that are recovered during journal recovery. They cannot fail and must be
    * processed to recreate the current state of the aggregate.
    */
  val receiveRecover: Receive = {
    case wevent: WrappedEvent[_] =>
      wevent.event match {
        case event: SpecimenLinkTypeAddedEvent   => recoverSpecimenLinkTypeAddedEvent  (event, wevent.userId, wevent.dateTime)
        case event: SpecimenLinkTypeUpdatedEvent => recoverSpecimenLinkTypeUpdatedEvent(event, wevent.userId, wevent.dateTime)
        case event: SpecimenLinkTypeRemovedEvent => recoverSpecimenLinkTypeRemovedEvent(event, wevent.userId, wevent.dateTime)

        case event => log.error(s"event not handled: $event")
      }

    case SnapshotOffer(_, snapshot: SnapshotState) =>
      snapshot.specimenLinkTypes.foreach{ ceType =>
        specimenLinkTypeRepository.put(ceType) }
  }

  /**
    * These are the commands that are requested. A command can fail, and will send the failure as a response
    * back to the user. Each valid command generates one or more events and is journaled.
    */
  val receiveCommand: Receive = {
    case cmd: AddSpecimenLinkTypeCmd    => processAddSpecimenLinkTypeCmd(cmd)
    case cmd: UpdateSpecimenLinkTypeCmd => processUpdateSpecimenLinkTypeCmd(cmd)
    case cmd: RemoveSpecimenLinkTypeCmd => processRemoveSpecimenLinkTypeCmd(cmd)

    case "snap" =>
      saveSnapshot(SnapshotState(specimenLinkTypeRepository.getValues.toSet))
      stash()

    case cmd => log.error(s"SpecimenLinkTypeProcessor: message not handled: $cmd")

  }

  def update
    (cmd: SpecimenLinkTypeModifyCommand)
    (fn: SpecimenLinkType => DomainValidation[SpecimenLinkType])
      : DomainValidation[SpecimenLinkType] = {
    for {
      processingType <- processingTypeRepository.getByKey(ProcessingTypeId(cmd.processingTypeId))
      slt <- specimenLinkTypeRepository.withId(
        ProcessingTypeId(cmd.processingTypeId), SpecimenLinkTypeId(cmd.id))
      notInUse <- checkNotInUse(slt)
      validVersion <- slt.requireVersion(cmd.expectedVersion)
      updatedSlt <- fn(slt)
    } yield updatedSlt
  }

  private def processAddSpecimenLinkTypeCmd
    (cmd: AddSpecimenLinkTypeCmd): Unit = {
    val timeNow = DateTime.now
    val processingTypeId = ProcessingTypeId(cmd.processingTypeId)
    val id = specimenLinkTypeRepository.nextIdentity

    val event = for {
      processingType <- processingTypeRepository.getByKey(processingTypeId)
      newItem <- SpecimenLinkType.create(
        processingTypeId,
        id,
        -1L,
        timeNow,
        cmd.expectedInputChange,
        cmd.expectedOutputChange,
        cmd.inputCount,
        cmd.outputCount,
        SpecimenGroupId(cmd.inputGroupId),
        SpecimenGroupId(cmd.outputGroupId),
        cmd.inputContainerTypeId.map(ContainerTypeId(_)),
        cmd.outputContainerTypeId.map(ContainerTypeId(_)),
        cmd.annotationTypeData)
      inputSpecimenGroup <- validSpecimenGroup(processingType, newItem.inputGroupId)
      outputSpecimenGroup <- validSpecimenGroup(processingType, newItem.outputGroupId)
      // FIXME: check that container types are valid
      validSpecimenGroups <- validateSpecimenGroups(newItem.inputGroupId, newItem.outputGroupId)
      validAnnotData <- validateAnnotationTypeData(processingTypeId, cmd.annotationTypeData)
      event <- SpecimenLinkTypeAddedEvent(
        processingTypeId      = cmd.processingTypeId,
        specimenLinkTypeId    = id.id,
        expectedInputChange   = Some(newItem.expectedInputChange.toDouble),
        expectedOutputChange  = Some(newItem.expectedOutputChange.toDouble),
        inputCount            = Some(newItem.inputCount),
        outputCount           = Some(newItem.outputCount),
        inputGroupId          = Some(newItem.inputGroupId.id),
        outputGroupId         = Some(newItem.outputGroupId.id),
        inputContainerTypeId  = newItem.inputContainerTypeId.map(_.id),
        outputContainerTypeId = newItem.outputContainerTypeId.map(_.id),
        annotationTypeData    = convertAnnotationTypeDataToEvent(newItem.annotationTypeData)).success
    } yield event

    process(event){ wevent =>
      recoverSpecimenLinkTypeAddedEvent(wevent.event, wevent.userId, wevent.dateTime)
    }
  }

  private def processUpdateSpecimenLinkTypeCmd
    (cmd: UpdateSpecimenLinkTypeCmd): Unit = {
    val timeNow = DateTime.now

    val v = update(cmd) { slt =>
      for {
        processingType <- processingTypeRepository.getByKey(slt.processingTypeId)
        updatedSlt <- slt.update(
          cmd.expectedInputChange,
          cmd.expectedOutputChange,
          cmd.inputCount,
          cmd.outputCount,
          SpecimenGroupId(cmd.inputGroupId),
          SpecimenGroupId(cmd.outputGroupId),
          cmd.inputContainerTypeId.map(ContainerTypeId(_)),
          cmd.outputContainerTypeId.map(ContainerTypeId(_)),
          cmd.annotationTypeData)

        inputSpecimenGroup <- validSpecimenGroup(processingType, updatedSlt.inputGroupId)
        outputSpecimenGroup <- validSpecimenGroup(processingType, updatedSlt.outputGroupId)
        // FIXME: check that container types are valid
        validSpecimenGroups <- validateSpecimenGroups(
          updatedSlt.inputGroupId, updatedSlt.outputGroupId, updatedSlt.id)
        validAtData <- validateAnnotationTypeData(processingType.id, cmd.annotationTypeData)
      } yield updatedSlt
    }

    val event = v.fold(
      err => DomainError(s"error $err occurred on $cmd").failureNel,
      slt => SpecimenLinkTypeUpdatedEvent(
        processingTypeId      = cmd.processingTypeId,
        specimenLinkTypeId    = slt.id.id,
        version               = Some(slt.version),
        expectedInputChange   = Some(slt.expectedInputChange.toDouble),
        expectedOutputChange  = Some(slt.expectedOutputChange.toDouble),
        inputCount            = Some(slt.inputCount),
        outputCount           = Some(slt.outputCount),
        inputGroupId          = Some(slt.inputGroupId.id),
        outputGroupId         = Some(slt.outputGroupId.id),
        inputContainerTypeId  = slt.inputContainerTypeId.map(_.id),
        outputContainerTypeId = slt.outputContainerTypeId.map(_.id),
        annotationTypeData    = convertAnnotationTypeDataToEvent(slt.annotationTypeData)).success
    )

    process(event){ wevent =>
      recoverSpecimenLinkTypeUpdatedEvent(wevent.event, wevent.userId, wevent.dateTime)
    }
  }

  private def processRemoveSpecimenLinkTypeCmd
    (cmd: RemoveSpecimenLinkTypeCmd): Unit = {
    val v = update(cmd) { slt => slt.success }

    val event = v.fold(
      err => DomainError(s"error $err occurred on $cmd").failureNel,
      pt =>  SpecimenLinkTypeRemovedEvent(cmd.processingTypeId, cmd.id).success
    )

    process(event){ wevent =>
      recoverSpecimenLinkTypeRemovedEvent(wevent.event, wevent.userId, wevent.dateTime)
    }
  }

  private def recoverSpecimenLinkTypeAddedEvent
    (event: SpecimenLinkTypeAddedEvent, userId: Option[UserId], dateTime: DateTime)
      : Unit = {
    specimenLinkTypeRepository.put(SpecimenLinkType(
      processingTypeId      = ProcessingTypeId(event.processingTypeId),
      id                    = SpecimenLinkTypeId(event.specimenLinkTypeId),
      version               = 0L,
      timeAdded             = dateTime,
      timeModified          = None,
      expectedInputChange   = event.getExpectedInputChange,
      expectedOutputChange  = event.getExpectedOutputChange,
      inputCount            = event.getInputCount,
      outputCount           = event.getOutputCount,
      inputGroupId          = SpecimenGroupId(event.getInputGroupId),
      outputGroupId         = SpecimenGroupId(event.getOutputGroupId),
      inputContainerTypeId  = event.inputContainerTypeId.map(ContainerTypeId(_)),
      outputContainerTypeId = event.outputContainerTypeId.map(ContainerTypeId(_)),
      annotationTypeData    = convertSpecimenLinkTypeAnnotationTypeDataFromEvent(event.annotationTypeData)))
    ()
  }

  private def recoverSpecimenLinkTypeUpdatedEvent
    (event: SpecimenLinkTypeUpdatedEvent, userId: Option[UserId], dateTime: DateTime)
      : Unit = {
    specimenLinkTypeRepository.getByKey(SpecimenLinkTypeId(event.specimenLinkTypeId)).fold(
      err => log.error(s"updating specimen link type from event failed: $err"),
      slt => {
        specimenLinkTypeRepository.put(slt.copy(
          version               = event.getVersion,
          timeModified          = Some(dateTime),
          expectedInputChange   = event.getExpectedInputChange,
          expectedOutputChange  = event.getExpectedOutputChange,
          inputCount            = event.getInputCount,
          outputCount           = event.getOutputCount,
          inputGroupId          = SpecimenGroupId(event.getInputGroupId),
          outputGroupId         = SpecimenGroupId(event.getOutputGroupId),
          inputContainerTypeId  = event.inputContainerTypeId.map(ContainerTypeId(_)),
          outputContainerTypeId = event.outputContainerTypeId.map(ContainerTypeId(_)),
          annotationTypeData    = convertSpecimenLinkTypeAnnotationTypeDataFromEvent(event.annotationTypeData)))
        ()
      }
    )
  }

  private def recoverSpecimenLinkTypeRemovedEvent
    (event: SpecimenLinkTypeRemovedEvent, userId: Option[UserId], dateTime: DateTime)
      : Unit = {
    specimenLinkTypeRepository.getByKey(SpecimenLinkTypeId(event.specimenLinkTypeId)).fold(
      err => log.error(s"updating specimen link type from event failed: $err"),
      slt => {
        specimenLinkTypeRepository.remove(slt)
        ()
      }
    )
  }

  private def validSpecimenGroup(
    processingType: ProcessingType,
    specimenGroupId : SpecimenGroupId): DomainValidation[Boolean] = {

    def studyIdMatches(specimenGroup: SpecimenGroup): DomainValidation[Boolean] = {
      if (specimenGroup.studyId == processingType.studyId) {
        true.success
      } else {
        DomainError("specimen group in wrong study").failureNel
      }
    }

    for {
      specimenGroup <- specimenGroupRepository.getByKey(specimenGroupId)
      studyMatch <- studyIdMatches(specimenGroup)
    } yield studyMatch
  }

  // should only have one specimen link type with these two specimen groups
  private def validateSpecimenGroupMatcher(
    inputGroupId: SpecimenGroupId,
    outputGroupId: SpecimenGroupId)(
    matcher: SpecimenLinkType => Boolean): DomainValidation[Boolean] = {
    val exists = specimenLinkTypeRepository.getValues.exists { slType => matcher(slType) }

    if (exists) {
      DomainError("specimen link type with specimen groups already exists").failureNel
    } else {
      true.success
    }
  }

  private def validateSpecimenGroups(
    inputGroupId: SpecimenGroupId,
    outputGroupId: SpecimenGroupId): DomainValidation[Boolean] = {
    validateSpecimenGroupMatcher(inputGroupId, outputGroupId) { slType =>
      (slType.inputGroupId == inputGroupId) && (slType.outputGroupId == outputGroupId)
    }
  }

  private def validateSpecimenGroups(
    inputGroupId: SpecimenGroupId,
    outputGroupId: SpecimenGroupId,
    specimenLinkTypeId: SpecimenLinkTypeId): DomainValidation[Boolean] = {
    validateSpecimenGroupMatcher(inputGroupId, outputGroupId) { slType =>
      (slType.id != specimenLinkTypeId) &&
      (slType.inputGroupId == inputGroupId) &&
      (slType.outputGroupId == outputGroupId)
    }
  }

  /**
    * Checks that each annotation type belongs to the same study as the collection event type. If
    * one or more annotation types are found that belong to a different study, they are returned in
    * the DomainError.
    */
  private def validateAnnotationTypeData(
    processingTypeId: ProcessingTypeId,
    annotationTypeData: List[SpecimenLinkTypeAnnotationTypeData]): DomainValidation[Boolean] = {

    def annotTypesValid(processingType: ProcessingType): DomainValidation[Boolean] = {
      val invalidSet = annotationTypeData.map(v => AnnotationTypeId(v.annotationTypeId)).map { id =>
        (id -> specimenLinkAnnotationTypeRepository.withId(processingType.studyId, id).isSuccess)
      }.filter(x => !x._2).map(_._1)

      if (invalidSet.isEmpty) true.success
      else DomainError("annotation type(s) do not belong to study: " + invalidSet.mkString(", ")).failureNel
    }

    for {
      processingType <- processingTypeRepository.getByKey(processingTypeId)
      valid <- annotTypesValid(processingType)
    } yield valid
  }

  def checkNotInUse(specimenLinkType: SpecimenLinkType): DomainValidation[SpecimenLinkType] = {
    // FIXME: this is a stub for now
    //
    // it needs to be replaced with the real check
    specimenLinkType.success
  }

}
