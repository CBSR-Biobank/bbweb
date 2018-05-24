package org.biobank.services.studies

import akka.actor._
import akka.persistence.{RecoveryCompleted, SaveSnapshotSuccess, SaveSnapshotFailure, SnapshotOffer}
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import org.biobank.domain._
import org.biobank.domain.annotations._
import org.biobank.domain.containers.ContainerTypeId
import org.biobank.domain.studies._
import org.biobank.infrastructure.commands.ProcessingTypeCommands._
import org.biobank.infrastructure.events.EventUtils
import org.biobank.services.{Processor, ServiceError, ServiceValidation, SnapshotWriter}
import play.api.libs.json._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

object ProcessingTypeProcessor {

  def props: Props = Props[ProcessingTypeProcessor]

  final case class SnapshotState(processingTypes: Set[ProcessingType])

  implicit val snapshotStateFormat: Format[SnapshotState] = Json.format[SnapshotState]

}

/**
 * The ProcessingTypeProcessor is responsible for maintaining state changes for all
 * [[domain.studies.ProcessingType]] aggregates. This particular processor uses
 * [[https://doc.akka.io/docs/akka/2.5/persistence.html Akka's PersistentActor]]. It receives Commands and if
 * they are valid they are converted to events and journaled, afterwhich the state of the
 * [[domain.studies.ProcessingType]] is updated.
 */
class ProcessingTypeProcessor @javax.inject.Inject() (
  val processingTypeRepository: ProcessingTypeRepository,
  val collectionEventTypeRepository: CollectionEventTypeRepository,
  val snapshotWriter: SnapshotWriter
)
    extends Processor {

  //import org.biobank.CommonValidations._
  import org.biobank.domain.studies.ProcessingType._
  import ProcessingTypeProcessor._
  import org.biobank.infrastructure.events.ProcessingTypeEvents._
  import org.biobank.infrastructure.events.ProcessingTypeEvents.ProcessingTypeEvent.EventType

  override def persistenceId: String = "processing-type-processor-id"

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var replyTo: Option[ActorRef] = None

  /**
   * These are the events that are recovered during journal recovery. They cannot fail and must be
   * processed to recreate the current state of the aggregate.
   */
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  val receiveRecover: Receive = {
    case event: ProcessingTypeEvent =>
      event.eventType match {
        case et: EventType.Added                          => applyAddedEvent(event)
        case et: EventType.NameUpdated                    => applyNameUpdatedEvent(event)
        case et: EventType.DescriptionUpdated             => applyDescriptionUpdatedEvent(event)
        case et: EventType.EnabledUpdated                 => applyEnabledUpdatedEvent(event)
        case et: EventType.ExpectedInputChangeUpdated     => applyExpectedChangeUpdatedEvent(event)
        case et: EventType.ExpectedOutputChangeUpdated    => applyExpectedChangeUpdatedEvent(event)
        case et: EventType.InputCountUpdated              => applyCountUpdatedEvent(event)
        case et: EventType.OutputCountUpdated             => applyCountUpdatedEvent(event)
        case et: EventType.InputContainerTypeUpdated      => applyContainerTypeUpdatedEvent(event)
        case et: EventType.OutputContainerTypeUpdated     => applyContainerTypeUpdatedEvent(event)
        case et: EventType.InputSpecimenDefinitionUpdated => applyInputSpecimenDefinitionUpdatedEvent(event)
        case et: EventType.AnnotationTypeAdded            => applyAnnotationTypeAddedEvent(event)
        case et: EventType.AnnotationTypeUpdated          => applyAnnotationTypeUpdatedEvent(event)
        case et: EventType.AnnotationTypeRemoved          => applyAnnotationTypeRemovedEvent(event)
        case et: EventType.Removed                        => applyRemovedEvent(event)

        case event => log.error(s"event not handled: $event")
      }

    case SnapshotOffer(_, snapshotFilename: String) =>
      applySnapshot(snapshotFilename)

    case RecoveryCompleted =>
      log.debug("ProcessingTypesProcessor: recovery completed")

    case msg =>
      log.error(s"message not handled: $msg")

  }

  /**
   * These are the commands that are requested. A command can fail, and will send the failure as a response
   * back to the user. Each valid command generates one or more events and is journaled.
   */
  @SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.Throw"))
  val receiveCommand: Receive = {
    case cmd: AddProcessingTypeCmd =>
      process(addCmdToEvent(cmd))(applyAddedEvent)

    case cmd: UpdateNameCmd =>
      processModifyCmd(cmd, updateNameCmdToEvent, applyNameUpdatedEvent)

    case cmd: UpdateDescriptionCmd =>
      processModifyCmd(cmd, updateDescriptionCmdToEvent, applyDescriptionUpdatedEvent)

    case cmd: UpdateEnabledCmd =>
      processModifyCmd(cmd, updateEnabledCmdToEvent, applyEnabledUpdatedEvent)

    case cmd: UpdateExpectedChangeCmd =>
      processModifyCmd(cmd, updateExpectedChangeCmdToEvent, applyExpectedChangeUpdatedEvent)

    case cmd: UpdateCountCmd =>
      processModifyCmd(cmd, updateInputCountCmdToEvent, applyCountUpdatedEvent)

    case cmd: UpdateContainerTypeCmd =>
      processModifyCmd(cmd, updateInputContainerTypeCmdToEvent, applyContainerTypeUpdatedEvent)

    case cmd: UpdateInputSpecimenDefinitionCmd =>
      processModifyCmd(cmd,
                       updateInputSpecimenDefinitionCmdToEvent,
                       applyInputSpecimenDefinitionUpdatedEvent)

    case cmd: UpdateOutputSpecimenDefinitionCmd =>
      processModifyCmd(cmd,
                       updateOutputSpecimenDefinitionCmdToEvent,
                       applyOutputSpecimenDefinitionUpdatedEvent)

    case cmd: AddProcessingTypeAnnotationTypeCmd =>
      processModifyCmd(cmd, addAnnotationTypeCmdToEvent, applyAnnotationTypeAddedEvent)

    case cmd: UpdateProcessingTypeAnnotationTypeCmd =>
      processModifyCmd(cmd, updateAnnotationTypeCmdToEvent, applyAnnotationTypeUpdatedEvent)

    case cmd: RemoveProcessingTypeAnnotationTypeCmd =>
      processModifyCmd(cmd, removeAnnotationTypeCmdToEvent, applyAnnotationTypeRemovedEvent)

    case cmd: RemoveProcessingTypeCmd =>
      processModifyCmd(cmd, removeCmdToEvent, applyRemovedEvent)

    case "snap" =>
      mySaveSnapshot
      replyTo = Some(sender())

    case SaveSnapshotSuccess(metadata) =>
      log.debug(s"snapshot saved successfully: ${metadata}")
      replyTo.foreach(_ ! akka.actor.Status.Success(s"snapshot saved: $metadata"))
      replyTo = None

    case SaveSnapshotFailure(metadata, reason) =>
      log.error(s"snapshot save error: ${metadata}")
      replyTo.foreach(_ ! akka.actor.Status.Failure(reason))
      replyTo = None

    case "terminate" =>
      context.stop(self)

    case cmd => log.error(s"ProcessingTypeProcessor: message not handled: $cmd")
  }

  private def mySaveSnapshot(): Unit = {
    val snapshotState = SnapshotState(processingTypeRepository.getValues.toSet)
    val filename = snapshotWriter.save(persistenceId, Json.toJson(snapshotState).toString)
    saveSnapshot(filename)
  }

  private def applySnapshot(filename: String): Unit = {
    log.debug(s"snapshot recovery file: $filename")
    val fileContents = snapshotWriter.load(filename)
    Json.parse(fileContents).validate[SnapshotState].fold(
      errors => log.error(s"could not apply snapshot: $filename: $errors"),
      snapshot =>  {
        log.debug(s"snapshot contains ${snapshot.processingTypes.size} processing types")
        snapshot.processingTypes.foreach(processingTypeRepository.put)
      }
    )
  }

  private def addCmdToEvent(cmd: AddProcessingTypeCmd): ServiceValidation[ProcessingTypeEvent] = {
    val cmdInput = cmd.specimenProcessing.input
    val studyId = StudyId(cmd.studyId)
    for {
      ptId            <- validNewIdentity(processingTypeRepository.nextIdentity, processingTypeRepository)
      nameValid       <- nameAvailable(cmd.name, studyId)
      definitionType  <- validateDefinitionType(cmdInput.definitionType)
      validDefinition <- validateSpecimenDefinition(definitionType,
                                                   cmdInput.entityId,
                                                   cmdInput.specimenDefinitionId)
      newItem         <- {
        val entityId: IdentifiedValueObject[_] =
          if (definitionType == collectedDefinition)
            CollectionEventTypeId(cmd.specimenProcessing.input.specimenDefinitionId)
          else
            ProcessingTypeId(cmd.specimenProcessing.input.specimenDefinitionId)

        val cmdInput = cmd.specimenProcessing.input
        val cmdOutput = cmd.specimenProcessing.output

        val input = studies.InputSpecimenProcessing(
          expectedChange       = cmdInput.expectedChange,
          count                = cmdInput.count,
          containerTypeId      = cmdInput.containerTypeId.map(ContainerTypeId.apply),
          definitionType       = definitionType,
          entityId             = entityId,
          specimenDefinitionId = SpecimenDefinitionId(cmdInput.specimenDefinitionId))

        val cmdDefinition = cmd.specimenProcessing.output.specimenDefinition
        val specimenDefinition = ProcessedSpecimenDefinition(
            id                      = SpecimenDefinitionId(""),
            slug                    = Slug(""),
            name                    = cmdDefinition.name,
            description             = cmdDefinition.description,
            units                   = cmdDefinition.units,
            anatomicalSourceType    = cmdDefinition.anatomicalSourceType,
            preservationType        = cmdDefinition.preservationType,
            preservationTemperature = cmdDefinition.preservationTemperature,
            specimenType            = cmdDefinition.specimenType)

        val output = studies.OutputSpecimenProcessing(
          expectedChange     = cmdOutput.expectedChange,
          count              = cmdOutput.count,
          containerTypeId    = cmdOutput.containerTypeId.map(ContainerTypeId.apply),
          specimenDefinition = specimenDefinition)

        val specimenProcessing = studies.SpecimenProcessing(input, output)

        ProcessingType.create(studyId            = StudyId(cmd.studyId),
                              id                 = ptId,
                              version            = 0L,
                              name               = cmd.name,
                              description        = cmd.description,
                              enabled            = cmd.enabled,
                              specimenProcessing = specimenProcessing,
                              annotationTypes    = Set.empty)
      }
    } yield {
      val timeStr = OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

      val event = ProcessingTypeEvent(ptId.id).update(
          _.studyId                             := studyId.id,
          _.sessionUserId                       := cmd.sessionUserId,
          _.time                                := timeStr,
          _.added.name                          := newItem.name,
          _.added.optionalDescription           := newItem.description,
          _.added.enabled                       := newItem.enabled,
          _.added.expectedInputChange           := newItem.specimenProcessing.input.expectedChange.toDouble,
          _.added.expectedOutputChange          := newItem.specimenProcessing.output.expectedChange.toDouble,
          _.added.inputCount                    := newItem.specimenProcessing.input.count,
          _.added.outputCount                   := newItem.specimenProcessing.output.count,
          _.added.optionalInputContainerTypeId  := cmd.specimenProcessing.input.containerTypeId,
          _.added.optionalOutputContainerTypeId := cmd.specimenProcessing.output.containerTypeId,
          _.added.outputSpecimenDefinition      := specimenDefinitionToEvent(newItem.specimenProcessing.output.specimenDefinition))

      if (definitionType == collectedDefinition) {
        event.update(
          _.added.collected.entityId             := cmd.specimenProcessing.input.entityId,
          _.added.collected.specimenDefinitionId := cmd.specimenProcessing.input.specimenDefinitionId)
      } else {
        event.update(
          _.added.processed.entityId             := cmd.specimenProcessing.input.entityId,
          _.added.processed.specimenDefinitionId := cmd.specimenProcessing.input.specimenDefinitionId)
      }

    }

  }

  private def updateNameCmdToEvent(cmd: UpdateNameCmd, processingType: ProcessingType)
      : ServiceValidation[ProcessingTypeEvent] = {
    processingType.withName(cmd.name)
      .map { _ =>
        ProcessingTypeEvent(processingType.id.id).update(
          _.studyId             := processingType.studyId.id,
          _.sessionUserId       := cmd.sessionUserId,
          _.time                := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
          _.nameUpdated.version := cmd.expectedVersion,
          _.nameUpdated.name    := cmd.name)
      }
  }

  private def updateDescriptionCmdToEvent(cmd: UpdateDescriptionCmd, processingType: ProcessingType)
      : ServiceValidation[ProcessingTypeEvent] = {
    processingType.withDescription(cmd.description)
      .map { _ =>
        val time = OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        ProcessingTypeEvent(processingType.id.id).update(
          _.studyId                                := processingType.studyId.id,
          _.sessionUserId                          := cmd.sessionUserId,
          _.time                                   := time,
          _.descriptionUpdated.version             := cmd.expectedVersion,
          _.descriptionUpdated.optionalDescription := cmd.description)
      }
  }

  private def updateEnabledCmdToEvent(cmd: UpdateEnabledCmd, processingType: ProcessingType)
      : ServiceValidation[ProcessingTypeEvent] = {
    val time = OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    ProcessingTypeEvent(processingType.id.id).update(
      _.studyId                := processingType.studyId.id,
      _.sessionUserId          := cmd.sessionUserId,
      _.time                   := time,
      _.enabledUpdated.version := cmd.expectedVersion,
      _.enabledUpdated.enabled := cmd.enabled).successNel[String]
  }

  private def updateExpectedChangeCmdToEvent(cmd: UpdateExpectedChangeCmd, processingType: ProcessingType)
      : ServiceValidation[ProcessingTypeEvent] = {
    val updated = if (cmd.inputType == specimenProcessingInput) {
        processingType.withExpectedInputChange(cmd.expectedChange)
      } else {
        processingType.withExpectedOutputChange(cmd.expectedChange)
      }

    updated.map { _ =>
      val time = OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
      val changeEvent = ProcessingTypeEvent.ExpectedChangeUpdated().update(
          _.version        := cmd.expectedVersion,
          _.expectedChange := cmd.expectedChange.toDouble)

      val event = ProcessingTypeEvent(processingType.id.id).update(
          _.studyId       := processingType.studyId.id,
          _.sessionUserId := cmd.sessionUserId,
          _.time          := time)

      if (cmd.inputType == specimenProcessingInput) {
        event.update(_.expectedInputChangeUpdated := changeEvent)
      } else {
        event.update(_.expectedOutputChangeUpdated := changeEvent)

      }
    }
  }

  private def updateInputCountCmdToEvent(cmd: UpdateCountCmd, processingType: ProcessingType)
      : ServiceValidation[ProcessingTypeEvent] = {
    val updated = if (cmd.inputType == specimenProcessingInput) {
        processingType.withInputCount(cmd.count)
      } else {
        processingType.withOutputCount(cmd.count)
      }

    updated.map { _ =>
      val time = OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
      val countEvent = ProcessingTypeEvent.CountUpdated().update(_.version := cmd.expectedVersion,
                                                                 _.count   := cmd.count)
      val event = ProcessingTypeEvent(processingType.id.id).update(
          _.studyId                      := processingType.studyId.id,
          _.sessionUserId                := cmd.sessionUserId,
          _.time                         := time)

      if (cmd.inputType == specimenProcessingInput) {
        event.update(_.inputCountUpdated := countEvent)
      } else {
        event.update(_.outputCountUpdated := countEvent)
      }
    }
  }

  private def updateInputContainerTypeCmdToEvent(cmd: UpdateContainerTypeCmd,
                                                 processingType: ProcessingType)
      : ServiceValidation[ProcessingTypeEvent] = {
    val containerTypeId = cmd.containerTypeId.map(ContainerTypeId.apply)
    val updated = if (cmd.inputType == specimenProcessingInput) {
        processingType.withInputContainerType(containerTypeId)
      } else {
        processingType.withOutputContainerType(containerTypeId)
      }

    updated.map { _ =>
      val time = OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
      val containerEvent = ProcessingTypeEvent.ContainerTypeUpdated().update(
          _.version                 := cmd.expectedVersion,
          _.optionalContainerTypeId := cmd.containerTypeId)

      val event = ProcessingTypeEvent(processingType.id.id).update(
          _.studyId                      := processingType.studyId.id,
          _.sessionUserId                := cmd.sessionUserId,
          _.time                         := time)

      if (cmd.inputType == specimenProcessingInput) {
        event.update(_.inputContainerTypeUpdated := containerEvent)
      } else {
        event.update(_.outputContainerTypeUpdated := containerEvent)
      }
    }
  }

  private def updateInputSpecimenDefinitionCmdToEvent(cmd: UpdateInputSpecimenDefinitionCmd,
                                                      processingType: ProcessingType)
      : ServiceValidation[ProcessingTypeEvent] = {
    val specimenDefinitionId = SpecimenDefinitionId(cmd.specimenDefinitionId)
    for {
      definitionType  <- validateDefinitionType(cmd.definitionType)
      validDefinition <- validateSpecimenDefinition(definitionType,
                                                   cmd.entityId,
                                                   cmd.specimenDefinitionId)
      updated <- {
        val entityId: IdentifiedValueObject[String] =
          if (definitionType == collectedDefinition) CollectionEventTypeId(cmd.specimenDefinitionId)
          else ProcessingTypeId(cmd.specimenDefinitionId)
        processingType.withInputSpecimenDefinition(definitionType, entityId, specimenDefinitionId)
      }
    } yield {
      val time = OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

      val input = ProcessingTypeEvent.InputSpecimenDefinitionType().update(
          _.definitionType       := cmd.definitionType,
          _.entityId             := cmd.entityId,
          _.specimenDefinitionId := cmd.specimenDefinitionId)

      val event = ProcessingTypeEvent(processingType.id.id).update(
          _.studyId                        := processingType.studyId.id,
          _.sessionUserId                  := cmd.sessionUserId,
          _.time                           := time)

      if (definitionType == collectedDefinition) {
        event.update(_.inputSpecimenDefinitionUpdated.collected := input)
      } else {
        event.update(_.inputSpecimenDefinitionUpdated.processed := input)
      }
    }
  }

  private def updateOutputSpecimenDefinitionCmdToEvent(cmd: UpdateOutputSpecimenDefinitionCmd,
                                                       processingType: ProcessingType)
      : ServiceValidation[ProcessingTypeEvent] = {
    // need to call ProcessedSpecimenDefinition.create so that a new Id is generated
    ProcessedSpecimenDefinition.create(
      name                    = cmd.specimenDefinition.name,
      description             = cmd.specimenDefinition.description,
      units                   = cmd.specimenDefinition.units,
      anatomicalSourceType    = cmd.specimenDefinition.anatomicalSourceType,
      preservationType        = cmd.specimenDefinition.preservationType,
      preservationTemperature = cmd.specimenDefinition.preservationTemperature,
      specimenType            = cmd.specimenDefinition.specimenType
    ).map { specimenDefinition =>
      val time = OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
      val sdEvent = ProcessingTypeEvent.SpecimenDefinition().update(
          _.id                      := specimenDefinition.id.id,
          _.name                    := specimenDefinition.name,
          _.optionalDescription     := specimenDefinition.description,
          _.units                   := specimenDefinition.units,
          _.anatomicalSourceType    := specimenDefinition.anatomicalSourceType.toString,
          _.preservationType        := specimenDefinition.preservationType.toString,
          _.preservationTemperature := specimenDefinition.preservationTemperature.toString,
          _.specimenType            := specimenDefinition.specimenType.toString)

      ProcessingTypeEvent(processingType.id.id).update(
        _.studyId                                            := processingType.studyId.id,
        _.sessionUserId                                      := cmd.sessionUserId,
        _.time                                               := time,
        _.outputSpecimenDefinitionUpdated.version            := cmd.expectedVersion,
        _.outputSpecimenDefinitionUpdated.specimenDefinition := sdEvent)
    }
  }

  private def addAnnotationTypeCmdToEvent(cmd: AddProcessingTypeAnnotationTypeCmd,
                                          processingType: ProcessingType)
      : ServiceValidation[ProcessingTypeEvent] = {
    for {
      annotationType <- {
        // need to call AnnotationType.create so that a new Id is generated
        AnnotationType.create(cmd.name,
                              cmd.description,
                              cmd.valueType,
                              cmd.maxValueCount,
                              cmd.options,
                              cmd.required)
      }
      updatedPt <- processingType.withAnnotationType(annotationType)
    } yield ProcessingTypeEvent(processingType.id.id).update(
      _.studyId                            := processingType.studyId.id,
      _.sessionUserId                      := cmd.sessionUserId,
      _.time                               := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
      _.annotationTypeAdded.version        := cmd.expectedVersion,
      _.annotationTypeAdded.annotationType := EventUtils.annotationTypeToEvent(annotationType))
  }

  private def updateAnnotationTypeCmdToEvent(cmd: UpdateProcessingTypeAnnotationTypeCmd,
                                             processingType: ProcessingType)
      : ServiceValidation[ProcessingTypeEvent] = {
    for {
      valid          <- AnnotationType.create(cmd.name,
                                              cmd.description,
                                              cmd.valueType,
                                              cmd.maxValueCount,
                                              cmd.options,
                                              cmd.required)
      annotationType <- valid.copy(id = AnnotationTypeId(cmd.annotationTypeId)).successNel[String]
      updatedPt      <- processingType.withAnnotationType(annotationType)
    } yield {
      ProcessingTypeEvent(processingType.id.id).update(
        _.studyId                              := processingType.studyId.id,
        _.sessionUserId                        := cmd.sessionUserId,
        _.time                                 := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        _.annotationTypeUpdated.version        := cmd.expectedVersion,
        _.annotationTypeUpdated.annotationType := EventUtils.annotationTypeToEvent(annotationType))
    }
  }

  private def removeAnnotationTypeCmdToEvent(cmd: RemoveProcessingTypeAnnotationTypeCmd,
                                             processingType: ProcessingType)
      : ServiceValidation[ProcessingTypeEvent] = {
    processingType.removeAnnotationType(AnnotationTypeId(cmd.annotationTypeId)) map { c =>
      ProcessingTypeEvent(processingType.id.id).update(
        _.studyId                        := processingType.studyId.id,
        _.sessionUserId                  := cmd.sessionUserId,
        _.time                           := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        _.annotationTypeRemoved.version  := cmd.expectedVersion,
        _.annotationTypeRemoved.id       := cmd.annotationTypeId)
    }
  }

  private def removeCmdToEvent(cmd: RemoveProcessingTypeCmd, processingType: ProcessingType)
      : ServiceValidation[ProcessingTypeEvent] = {
    // FIXME: enable this check
    // if (processingTypeRepository.processingTypeInUse(processingType.id)) {
    //   EntityInUse(s"processing type in use: ${processingType.id}").failureNel[ProcessingTypeEvent]
    // } else {
    ProcessingTypeEvent(processingType.id.id).update(
      _.studyId               := processingType.studyId.id,
      _.sessionUserId         := cmd.sessionUserId,
      _.time                  := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
      _.removed.version       := cmd.expectedVersion).successNel[String]
    // }
  }

  private def applyAddedEvent(event: ProcessingTypeEvent): Unit = {
    if (!event.eventType.isAdded) {
      log.error(s"invalid event type: $event")
    } else {
      val addedEvent = event.getAdded
      val inputContainerTypeId = addedEvent.inputContainerTypeId.map(ContainerTypeId.apply)
      val outputContainerTypeId = addedEvent.outputContainerTypeId.map(ContainerTypeId.apply)

      val input =
        if (addedEvent.inputSpecimenDefinitionType.isCollected) {
          val collected = addedEvent.getCollected
          studies.InputSpecimenProcessing(
            expectedChange       = addedEvent.getExpectedInputChange,
            count                = addedEvent.getInputCount,
            containerTypeId      = inputContainerTypeId,
            definitionType       = collectedDefinition,
            entityId             = CollectionEventTypeId(collected.getEntityId),
            specimenDefinitionId = SpecimenDefinitionId(collected.getSpecimenDefinitionId))
        } else {
          val processed = addedEvent.getProcessed
          studies.InputSpecimenProcessing(
            expectedChange       = addedEvent.getExpectedInputChange,
            count                = addedEvent.getInputCount,
            containerTypeId      = inputContainerTypeId,
            definitionType       = processedDefinition,
            entityId             = ProcessingTypeId(processed.getEntityId),
            specimenDefinitionId = SpecimenDefinitionId(processed.getSpecimenDefinitionId))
        }

      val output = studies.OutputSpecimenProcessing(
          expectedChange     = addedEvent.getExpectedOutputChange,
          count              = addedEvent.getOutputCount,
          containerTypeId    = outputContainerTypeId,
          specimenDefinition = specimenDefinitionFromEvent(addedEvent.getOutputSpecimenDefinition))

      val v = ProcessingType.create(
          studyId                = StudyId(event.getStudyId),
          id                     = ProcessingTypeId(event.id),
          version                = 0L,
          name                   = addedEvent.getName,
          description            = addedEvent.description,
          enabled                = addedEvent.getEnabled,
          specimenProcessing     = studies.SpecimenProcessing(input, output),
          annotationTypes        = Set.empty)

      if (v.isFailure) {
        log.error(s"could not add collection event type from event: $v")
      }

      v.foreach { ct =>
        val timeAdded = OffsetDateTime.parse(event.getTime)
        val slug = processingTypeRepository.uniqueSlugFromStr(ct.name)
        processingTypeRepository.put(ct.copy(slug = slug, timeAdded = timeAdded))
      }
    }
  }

  private def applyNameUpdatedEvent(event: ProcessingTypeEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isNameUpdated,
                           event.getNameUpdated.getVersion) { (processingType, eventTime) =>
      val v = processingType.withName(event.getNameUpdated.getName)
      v.foreach { pt =>
        val updated = pt.copy(slug         = processingTypeRepository.uniqueSlugFromStr(pt.name),
                              timeModified = Some(eventTime))
        processingTypeRepository.put(updated)
      }
      v.map(_ => true)
    }
  }

  private def applyDescriptionUpdatedEvent(event: ProcessingTypeEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isDescriptionUpdated,
                           event.getDescriptionUpdated.getVersion) { (processingType, eventTime) =>
      val v = processingType.withDescription(event.getDescriptionUpdated.description)
      v.foreach { pt =>
        processingTypeRepository.put(pt.copy(timeModified = Some(eventTime)))
      }
      v.map(_ => true)
    }
  }

  private def applyEnabledUpdatedEvent(event: ProcessingTypeEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isEnabledUpdated,
                           event.getEnabledUpdated.getVersion) { (processingType, eventTime) =>
      val updated = if (event.getEnabledUpdated.getEnabled) processingType.enable()
                    else processingType.disable()

      processingTypeRepository.put(updated.copy(timeModified = Some(eventTime)))
      true.successNel[String]
    }
  }

  private def applyExpectedChangeUpdatedEvent(event: ProcessingTypeEvent): Unit = {
    val eventType = (event.eventType.isExpectedInputChangeUpdated
                       || event.eventType.isExpectedOutputChangeUpdated)
    val isInput = event.eventType.isExpectedInputChangeUpdated
    val version   = if (isInput)
                      event.getExpectedInputChangeUpdated.getVersion
                    else
                      event.getExpectedOutputChangeUpdated.getVersion
    val change    = if (isInput)
                      event.getExpectedInputChangeUpdated.getExpectedChange
                    else
                      event.getExpectedOutputChangeUpdated.getExpectedChange

    onValidEventAndVersion(event, eventType, version) { (processingType, eventTime) =>
      val v = if (isInput) {
          processingType.withExpectedInputChange(change)
        } else {
          processingType.withExpectedOutputChange(change)
        }
      v.foreach { pt =>
        processingTypeRepository.put(pt.copy(timeModified = Some(eventTime)))
      }
      v.map(_ => true)
    }
  }

  private def applyCountUpdatedEvent(event: ProcessingTypeEvent): Unit = {
    val eventType = event.eventType.isInputCountUpdated || event.eventType.isOutputCountUpdated
    val isInput = event.eventType.isInputCountUpdated
    val version   = if (isInput) event.getInputCountUpdated.getVersion
                    else event.getOutputCountUpdated.getVersion
    val count     = if (isInput) event.getInputCountUpdated.getCount
                    else event.getOutputCountUpdated.getCount

    onValidEventAndVersion(event, eventType, version) { (processingType, eventTime) =>
      val v = if (isInput) {
          processingType.withInputCount(count)
        } else {
          processingType.withOutputCount(count)
        }

      v.foreach { pt =>
        processingTypeRepository.put(pt.copy(timeModified = Some(eventTime)))
      }
      v.map(_ => true)
    }
  }

  private def applyContainerTypeUpdatedEvent(event: ProcessingTypeEvent): Unit = {
    val eventType = (event.eventType.isInputContainerTypeUpdated
                       || event.eventType.isOutputContainerTypeUpdated)
    val isInput = event.eventType.isInputContainerTypeUpdated
    val version         = if (isInput) event.getInputContainerTypeUpdated.getVersion
                          else event.getOutputContainerTypeUpdated.getVersion
    val containerTypeId = if (isInput) event.getInputContainerTypeUpdated.containerTypeId
                          else event.getOutputContainerTypeUpdated.containerTypeId

    onValidEventAndVersion(event, eventType, version) { (processingType, eventTime) =>
      val v = if (isInput) {
          processingType.withInputContainerType(containerTypeId.map(ContainerTypeId.apply))
        } else {
          processingType.withOutputContainerType(containerTypeId.map(ContainerTypeId.apply))
        }

      v.foreach { pt =>
        processingTypeRepository.put(pt.copy(timeModified = Some(eventTime)))
      }
      v.map(_ => true)
    }
  }

  private def applyInputSpecimenDefinitionUpdatedEvent(event: ProcessingTypeEvent): Unit = {
    onValidEventAndVersion(
      event,
      event.eventType.isInputSpecimenDefinitionUpdated,
      event.getInputSpecimenDefinitionUpdated.getVersion
    ) { (processingType, eventTime) =>

      val inputEvent = event.getInputSpecimenDefinitionUpdated
      val v = for {
          definitionType <- {
            if (inputEvent.specimenDefinitionType.isCollected) {
              ProcessingType.collectedDefinition.successNel[String]
            } else if (inputEvent.specimenDefinitionType.isProcessed) {
              ProcessingType.processedDefinition.successNel[String]
            } else {
              ServiceError(s"invalid input specimen definition type: $event")
                .failureNel[InputSpecimenDefinitionType]
            }
          }
          updated <- {
            if (definitionType == ProcessingType.collectedDefinition) {
              processingType.withInputSpecimenDefinition(
                definitionType,
                CollectionEventTypeId(inputEvent.getCollected.getEntityId),
                SpecimenDefinitionId(inputEvent.getCollected.getSpecimenDefinitionId))
            } else {
              processingType.withInputSpecimenDefinition(
                definitionType,
                ProcessingTypeId(inputEvent.getProcessed.getEntityId),
                SpecimenDefinitionId(inputEvent.getProcessed.getSpecimenDefinitionId))
            }
          }
        } yield updated
      v.foreach { pt =>
        processingTypeRepository.put(pt.copy(timeModified = Some(eventTime)))
      }
      v.map(_ => true)
    }
  }

  private def applyOutputSpecimenDefinitionUpdatedEvent(event: ProcessingTypeEvent): Unit = {
    onValidEventAndVersion(
      event,
      event.eventType.isOutputSpecimenDefinitionUpdated,
      event.getOutputSpecimenDefinitionUpdated.getVersion
    ) { (processingType, eventTime) =>
      val outputEvent = event.getOutputSpecimenDefinitionUpdated

      val specimenDefinition = specimenDefinitionFromEvent(outputEvent.getSpecimenDefinition)

      val v = for {
        valid <- ProcessedSpecimenDefinition.validate(specimenDefinition)
        updated <- processingType.withOutputSpecimenDefinition(specimenDefinition)
      } yield updated

      v.foreach { pt =>
        processingTypeRepository.put(pt.copy(timeModified = Some(eventTime)))
      }
      v.map(_ => true)
    }
  }

  private def applyAnnotationTypeAddedEvent(event: ProcessingTypeEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isAnnotationTypeAdded,
                           event.getAnnotationTypeAdded.getVersion) { (processingType, eventTime) =>
      val eventAnnotationType = event.getAnnotationTypeAdded.getAnnotationType
      storeIfValid(processingType.withAnnotationType(EventUtils.annotationTypeFromEvent(eventAnnotationType)), eventTime)
    }
  }

  private def applyAnnotationTypeUpdatedEvent(event: ProcessingTypeEvent) : Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isAnnotationTypeUpdated,
                           event.getAnnotationTypeUpdated.getVersion) { (processingType, eventTime) =>
      val eventAnnotationType = event.getAnnotationTypeUpdated.getAnnotationType
      val annotationType = AnnotationType(AnnotationTypeId(eventAnnotationType.getId),
                                          Slug(eventAnnotationType.getName),
                                          eventAnnotationType.getName,
                                          eventAnnotationType.description,
                                          AnnotationValueType.withName(eventAnnotationType.getValueType),
                                          eventAnnotationType.maxValueCount,
                                          eventAnnotationType.options,
                                          eventAnnotationType.getRequired)
      storeIfValid(processingType.withAnnotationType(annotationType), eventTime)
    }
  }

  private def applyAnnotationTypeRemovedEvent(event: ProcessingTypeEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isAnnotationTypeRemoved,
                           event.getAnnotationTypeRemoved.getVersion) { (processingType, eventTime) =>
      storeIfValid(
        processingType.removeAnnotationType(AnnotationTypeId(event.getAnnotationTypeRemoved.getId)),
        eventTime)
    }
  }

  private def onValidEventAndVersion(
    event:        ProcessingTypeEvent,
    eventType:    Boolean,
    eventVersion: Long
  ) (
    applyEvent: (ProcessingType, OffsetDateTime) => ServiceValidation[Boolean]
  ): Unit = {
    if (!eventType) {
      log.error(s"invalid event type: $event")
    } else {
      processingTypeRepository.getByKey(ProcessingTypeId(event.id)).fold(
        err => log.error(s"processing type from event does not exist: $err"),
        pt => {
          if (pt.version != eventVersion) {
            log.error(s"event version check failed: pt version: ${pt.version}, event: $event")
          } else {
            val eventTime = OffsetDateTime.parse(event.getTime)
            val update = applyEvent(pt, eventTime)

            if (update.isFailure) {
              log.error(s"processing type update from event failed: $update")
            }
          }
        }
      )
    }
  }

  private def processModifyCmd[T <: ProcessingTypeModifyCommand](
    cmd:           T,
    validateCmd:   (T, ProcessingType) => ServiceValidation[ProcessingTypeEvent],
    applyEvent:    ProcessingTypeEvent => Unit
  ): Unit = {
    val event = for {
        processingType <- processingTypeRepository.getByKey(ProcessingTypeId(cmd.id))
        validVersion   <- processingType.requireVersion(cmd.expectedVersion)
        event          <- validateCmd(cmd, processingType)
      } yield event

    process(event)(applyEvent)
  }

  private def applyRemovedEvent(event: ProcessingTypeEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isRemoved,
                           event.getRemoved.getVersion) { (pt, _) =>
      val v = processingTypeRepository.getByKey(pt.id)
      v.foreach(processingTypeRepository.remove)
      v.map(_ => true)
    }
  }

  val ErrMsgNameExists: String = "processing type with name already exists"

  def checkNotInUse(processingType: ProcessingType): ServiceValidation[ProcessingType] = {
    // FIXME: this is a stub for now
    //
    // it needs to be replaced with the real check
    processingType.successNel[String]
  }

  private def nameAvailable(name: String, studyId: StudyId): ServiceValidation[Boolean] = {
    nameAvailableMatcher(name, processingTypeRepository, ErrMsgNameExists) { item =>
      (item.name == name) && (item.studyId == studyId)
    }
  }

  private def specimenDefinitionToEvent(specimenDefinition: ProcessedSpecimenDefinition):
      ProcessingTypeEvent.SpecimenDefinition = {
    ProcessingTypeEvent.SpecimenDefinition().update(
      _.id                      := specimenDefinition.id.id,
      _.name                    := specimenDefinition.name,
      _.optionalDescription     := specimenDefinition.description,
      _.units                   := specimenDefinition.units,
      _.anatomicalSourceType    := specimenDefinition.anatomicalSourceType.toString,
      _.preservationType        := specimenDefinition.preservationType.toString,
      _.preservationTemperature := specimenDefinition.preservationTemperature.toString,
      _.specimenType            := specimenDefinition.specimenType.toString
    )
  }

  private def specimenDefinitionFromEvent(event: ProcessingTypeEvent.SpecimenDefinition)
      : ProcessedSpecimenDefinition = {
    ProcessedSpecimenDefinition(
      id                      = SpecimenDefinitionId(event.getId),
      slug                    = Slug(event.getName),
      name                    = event.getName,
      description             = event.description,
      units                   = event.getUnits,
      anatomicalSourceType    = AnatomicalSourceType.withName(event.getAnatomicalSourceType),
      preservationType        = PreservationType.withName(event.getPreservationType),
      preservationTemperature = PreservationTemperature.withName(event.getPreservationTemperature),
      specimenType            = SpecimenType.withName(event.getSpecimenType)
    )
  }

  private def validateDefinitionType(str: String): ServiceValidation[InputSpecimenDefinitionType] = {
    if (str == collectedDefinition.id) collectedDefinition.successNel[String]
    else if (str == processedDefinition.id) processedDefinition.successNel[String]
    else ServiceError(s"invalid input specimen definition type: $str")
      .failureNel[InputSpecimenDefinitionType]
  }

  private def validateSpecimenDefinition(definitionType:       InputSpecimenDefinitionType,
                                         entityId:             String,
                                         specimenDefinitionId: String)
      : ServiceValidation[Boolean] = {
    val definitionId = SpecimenDefinitionId(specimenDefinitionId)
    if (definitionType == collectedDefinition) {
      collectionEventTypeRepository.getByKey(CollectionEventTypeId(entityId)).flatMap { eventType =>
        eventType.specimenDefinition(definitionId).map { _ => true }
      }
    } else {
      processingTypeRepository.getByKey(ProcessingTypeId(entityId)).flatMap { processingType =>
        if (processingType.specimenProcessing.output.specimenDefinition.id == definitionId) {
          true.successNel[String]
        } else {
          ServiceError(s"IdNotFound: specimen definition id: $definitionId").failureNel[Boolean]
        }
      }
    }
  }

  private def storeIfValid(validation: ServiceValidation[ProcessingType],
                           eventTime: OffsetDateTime): ServiceValidation[Boolean] = {
    validation.foreach { c =>
      processingTypeRepository.put(c.copy(timeModified = Some(eventTime)))
    }
    validation.map(_ => true)
  }

  private def init(): Unit = {
    processingTypeRepository.init
  }

  init
}
