package org.biobank.service.study

import org.biobank.domain._
import org.biobank.domain.study.{
  ProcessingType,
  ProcessingTypeId,
  ProcessingTypeRepositoryComponent,
  SpecimenLinkType,
  SpecimenLinkTypeId,
  SpecimenLinkTypeRepositoryComponent,
  SpecimenLinkAnnotationTypeRepositoryComponent,
  SpecimenGroup,
  SpecimenGroupId,
  SpecimenGroupRepositoryComponent
}
import org.slf4j.LoggerFactory
import org.biobank.service._
import org.biobank.infrastructure._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._

import akka.persistence.SnapshotOffer
import org.joda.time.DateTime
import scalaz._
import scalaz.Scalaz._

trait SpecimenLinkTypeProcessorComponent {
  self: SpecimenLinkTypeRepositoryComponent
      with SpecimenLinkAnnotationTypeRepositoryComponent
      with ProcessingTypeRepositoryComponent
      with SpecimenGroupRepositoryComponent =>

  /**
    * The SpecimenLinkTypeProcessor is responsible for maintaining state changes for all
    * [[org.biobank.domain.study.SpecimenLinkType]] aggregates. This particular processor uses
    * Akka-Persistence's [[akka.persistence.PersistentActor]]. It receives Commands and if valid will persist
    * the generated events, afterwhich it will updated the current state of the
    * [[org.biobank.domain.study.SpecimenLinkType]] being processed.
    *
    * It is a child actor of [[org.biobank.service.study.StudiesProcessorComponent.StudiesProcessor]].
    */
  class SpecimenLinkTypeProcessor extends Processor {

    override def persistenceId = "specimen-link-type-processor-id"

    case class SnapshotState(specimenLinkTypes: Set[SpecimenLinkType])

    /**
      * These are the events that are recovered during journal recovery. They cannot fail and must be
      * processed to recreate the current state of the aggregate.
      */
    val receiveRecover: Receive = {
      case event: SpecimenLinkTypeAddedEvent => recoverEvent(event)
      case event: SpecimenLinkTypeUpdatedEvent => recoverEvent(event)
      case event: SpecimenLinkTypeRemovedEvent => recoverEvent(event)

      case SnapshotOffer(_, snapshot: SnapshotState) =>
        snapshot.specimenLinkTypes.foreach{ ceType =>
          specimenLinkTypeRepository.put(ceType) }
    }

    /**
      * These are the commands that are requested. A command can fail, and will send the failure as a response
      * back to the user. Each valid command generates one or more events and is journaled.
      */
    val receiveCommand: Receive = {
      case cmd: AddSpecimenLinkTypeCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }
      case cmd: UpdateSpecimenLinkTypeCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }
      case cmd: RemoveSpecimenLinkTypeCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }
    }

    def update
      (cmd: SpecimenLinkTypeCommand)
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

    private def validateCmd(
      cmd: AddSpecimenLinkTypeCmd): DomainValidation[SpecimenLinkTypeAddedEvent] = {
      val timeNow = DateTime.now
      val processingTypeId = ProcessingTypeId(cmd.processingTypeId)
      val id = specimenLinkTypeRepository.nextIdentity

      for {
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
          cmd.processingTypeId,
          id.id,
          timeNow,
          newItem.expectedInputChange,
          newItem.expectedOutputChange,
          newItem.inputCount,
          newItem.outputCount,
          newItem.inputGroupId,
          newItem.outputGroupId,
          newItem.inputContainerTypeId,
          newItem.outputContainerTypeId,
          newItem.annotationTypeData).success
      } yield event
    }

    private def validateCmd(cmd: UpdateSpecimenLinkTypeCmd)
        : DomainValidation[SpecimenLinkTypeUpdatedEvent] = {
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

      v.fold(
        err => DomainError(s"error $err occurred on $cmd").failNel,
        slt => SpecimenLinkTypeUpdatedEvent(
          cmd.processingTypeId,
          slt.id.id,
          slt.version,
          timeNow,
          slt.expectedInputChange,
          slt.expectedOutputChange,
          slt.inputCount,
          slt.outputCount,
          slt.inputGroupId,
          slt.outputGroupId,
          slt.inputContainerTypeId,
          slt.outputContainerTypeId,
          slt.annotationTypeData).success
      )
    }

    private def validateCmd(
      cmd: RemoveSpecimenLinkTypeCmd): DomainValidation[SpecimenLinkTypeRemovedEvent] = {
      val v = update(cmd) { slt => slt.success }

      v.fold(
        err => DomainError(s"error $err occurred on $cmd").failNel,
        pt =>  SpecimenLinkTypeRemovedEvent(cmd.processingTypeId, cmd.id).success
      )
    }

    private def recoverEvent(event: SpecimenLinkTypeAddedEvent): Unit = {
      specimenLinkTypeRepository.put(SpecimenLinkType(
        ProcessingTypeId(event.processingTypeId),
        SpecimenLinkTypeId(event.specimenLinkTypeId),
        0L,
        event.dateTime,
        None,
        event.expectedInputChange,
        event.expectedOutputChange,
        event.inputCount,
        event.outputCount,
        event.inputGroupId,
        event.outputGroupId,
        event.inputContainerTypeId,
        event.outputContainerTypeId,
        event.annotationTypeData))
      ()
    }

    private def recoverEvent(event: SpecimenLinkTypeUpdatedEvent): Unit = {
      specimenLinkTypeRepository.getByKey(SpecimenLinkTypeId(event.specimenLinkTypeId)).fold(
        err => throw new IllegalStateException(s"updating specimen link type from event failed: $err"),
        slt => specimenLinkTypeRepository.put(slt.copy(
          version               = event.version,
          lastUpdateDate        = Some(event.dateTime),
          expectedInputChange   = event.expectedInputChange,
          expectedOutputChange  = event.expectedOutputChange,
          inputCount            = event.inputCount,
          outputCount           = event.outputCount,
          inputGroupId          = event.inputGroupId,
          outputGroupId         = event.outputGroupId,
          inputContainerTypeId  = event.inputContainerTypeId,
          outputContainerTypeId = event.outputContainerTypeId,
          annotationTypeData    = event.annotationTypeData))
      )
      ()
    }

    private def recoverEvent(event: SpecimenLinkTypeRemovedEvent): Unit = {
      specimenLinkTypeRepository.getByKey(SpecimenLinkTypeId(event.specimenLinkTypeId)).fold(
        err => throw new IllegalStateException(s"updating specimen link type from event failed: $err"),
        slt => specimenLinkTypeRepository.remove(slt)
      )
      ()
    }

    private def validSpecimenGroup(
      processingType: ProcessingType,
      specimenGroupId : SpecimenGroupId): DomainValidation[Boolean] = {

      def studyIdMatches(specimenGroup: SpecimenGroup): DomainValidation[Boolean] = {
        if (specimenGroup.studyId == processingType.studyId) {
          true.success
        } else {
          DomainError("specimen group in wrong study").failNel
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
        DomainError("specimen link type with specimen groups already exists").failNel
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
        else DomainError("annotation type(s) do not belong to study: " + invalidSet.mkString(", ")).failNel
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

}
