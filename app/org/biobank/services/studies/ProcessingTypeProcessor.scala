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

  import org.biobank.CommonValidations._
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
        case et: EventType.Added                           => applyAddedEvent(event)
        case et: EventType.NameUpdated                     => applyNameUpdatedEvent(event)
        case et: EventType.DescriptionUpdated              => applyDescriptionUpdatedEvent(event)
        case et: EventType.EnabledUpdated                  => applyEnabledUpdatedEvent(event)
        case et: EventType.InputSpecimenProcessingUpdated  => applyInputSpecimenProcessingUpdatedEvent(event)
        case et: EventType.OutputSpecimenProcessingUpdated => applyOutputSpecimenProcessingUpdatedEvent(event)
        case et: EventType.AnnotationTypeAdded             => applyAnnotationTypeAddedEvent(event)
        case et: EventType.AnnotationTypeUpdated           => applyAnnotationTypeUpdatedEvent(event)
        case et: EventType.AnnotationTypeRemoved           => applyAnnotationTypeRemovedEvent(event)
        case et: EventType.Removed                         => applyRemovedEvent(event)

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

    case cmd: UpdateInputSpecimenProcessingCmd =>
      processModifyCmd(cmd,
                       updateInputSpecimenProcessingCmdToEvent,
                       applyInputSpecimenProcessingUpdatedEvent)

    case cmd: UpdateOutputSpecimenProcessingCmd =>
      processModifyCmd(cmd,
                       updateOutputSpecimenProcessingCmdToEvent,
                       applyOutputSpecimenProcessingUpdatedEvent)

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
    val studyId = StudyId(cmd.studyId)

    for {
      ptId               <- validNewIdentity(processingTypeRepository.nextIdentity,
                                            processingTypeRepository)
      nameValid          <- nameAvailable(cmd.name, studyId)
      input              <- studies.InputSpecimenProcessing.create(
        expectedChange       = cmd.input.expectedChange,
        count                = cmd.input.count,
        containerTypeId      = cmd.input.containerTypeId.map(ContainerTypeId.apply),
        definitionType       = cmd.input.definitionType,
        entityId             = cmd.input.entityId,
        specimenDefinitionId = cmd.input.specimenDefinitionId)

      specimenDefinition <- {
        val cmdDefinition = cmd.output.specimenDefinition

        ProcessedSpecimenDefinition.create(
          name                    = cmdDefinition.name,
          description             = cmdDefinition.description,
          units                   = cmdDefinition.units,
          anatomicalSourceType    = cmdDefinition.anatomicalSourceType,
          preservationType        = cmdDefinition.preservationType,
          preservationTemperature = cmdDefinition.preservationTemperature,
          specimenType            = cmdDefinition.specimenType)
      }

      output <- studies.OutputSpecimenProcessing.create(
        expectedChange     = cmd.output.expectedChange,
        count              = cmd.output.count,
        containerTypeId    = cmd.output.containerTypeId.map(ContainerTypeId.apply),
        specimenDefinition = specimenDefinition)

      newItem <- {
        ProcessingType.create(studyId         = StudyId(cmd.studyId),
                              id              = ptId,
                              version         = 0L,
                              name            = cmd.name,
                              description     = cmd.description,
                              enabled         = cmd.enabled,
                              input           = input,
                              output          = output,
                              annotationTypes = Set.empty)
      }
    } yield {
      val timeStr = OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
      val inputEvent = ProcessingTypeEvent.InputSpecimenProcessing().update(
          _.expectedChange          := newItem.input.expectedChange.toDouble,
          _.count                   := newItem.input.count,
          _.optionalContainerTypeId := cmd.input.containerTypeId,
          _.entityId                := cmd.input.entityId,
          _.specimenDefinitionId    := cmd.input.specimenDefinitionId)

      val outputEvent = ProcessingTypeEvent.OutputSpecimenProcessing().update(
          _.expectedChange          := newItem.output.expectedChange.toDouble,
          _.count                   := newItem.output.count,
          _.optionalContainerTypeId := cmd.output.containerTypeId,
          _.specimenDefinition      := specimenDefinitionToEvent(newItem.output.specimenDefinition))

      val event = ProcessingTypeEvent(ptId.id).update(
          _.studyId                        := studyId.id,
          _.sessionUserId                  := cmd.sessionUserId,
          _.time                           := timeStr,
          _.added.name                     := newItem.name,
          _.added.optionalDescription      := newItem.description,
          _.added.enabled                  := newItem.enabled,
          _.added.outputSpecimenProcessing := outputEvent)

      if (input.definitionType == collectedDefinition) {
        event.update(_.added.collected := inputEvent)
      } else {
        event.update(_.added.processed := inputEvent)
      }
    }

  }

  private def updateNameCmdToEvent(cmd: UpdateNameCmd, processingType: ProcessingType)
      : ServiceValidation[ProcessingTypeEvent] = {
    for {
      nameValid <- nameAvailable(cmd.name, processingType.studyId)
      updated   <- processingType.withName(cmd.name)
    } yield ProcessingTypeEvent(processingType.id.id).update(
      _.studyId             := processingType.studyId.id,
      _.sessionUserId       := cmd.sessionUserId,
      _.time                := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
      _.nameUpdated.version := cmd.expectedVersion,
      _.nameUpdated.name    := cmd.name)
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

  private def updateInputSpecimenProcessingCmdToEvent(cmd: UpdateInputSpecimenProcessingCmd,
                                                      processingType: ProcessingType)
      : ServiceValidation[ProcessingTypeEvent] = {
    for {
      input <- studies.InputSpecimenProcessing.create(
        expectedChange       = cmd.expectedChange,
        count                = cmd.count,
        containerTypeId      = cmd.containerTypeId.map(ContainerTypeId.apply),
        definitionType       = cmd.definitionType,
        entityId             = cmd.entityId,
        specimenDefinitionId = cmd.specimenDefinitionId)
      valid   <- validateInputSpecimenProcessing(input)
      updated <- processingType.withInputSpecimenProcessing(input)
    } yield {
      val time = OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

      val inputEvent = ProcessingTypeEvent.InputSpecimenProcessing().update(
          _.expectedChange          := cmd.expectedChange.toDouble,
          _.count                   := cmd.count,
          _.optionalContainerTypeId := cmd.containerTypeId,
          _.definitionType          := cmd.definitionType,
          _.entityId                := cmd.entityId,
          _.specimenDefinitionId    := cmd.specimenDefinitionId)

      val event = ProcessingTypeEvent(processingType.id.id).update(
          _.studyId                                := processingType.studyId.id,
          _.sessionUserId                          := cmd.sessionUserId,
          _.time                                   := time,
          _.inputSpecimenProcessingUpdated.version := cmd.expectedVersion)

      if (input.definitionType == collectedDefinition) {
        event.update(_.inputSpecimenProcessingUpdated.collected := inputEvent)
      } else {
        event.update(_.inputSpecimenProcessingUpdated.processed := inputEvent)
      }
    }
  }

  private def updateOutputSpecimenProcessingCmdToEvent(cmd: UpdateOutputSpecimenProcessingCmd,
                                                       processingType: ProcessingType)
      : ServiceValidation[ProcessingTypeEvent] = {
    for {
      validSpecimenDefinition <- ProcessedSpecimenDefinition.create(
        name                    = cmd.specimenDefinition.name,
        description             = cmd.specimenDefinition.description,
        units                   = cmd.specimenDefinition.units,
        anatomicalSourceType    = cmd.specimenDefinition.anatomicalSourceType,
        preservationType        = cmd.specimenDefinition.preservationType,
        preservationTemperature = cmd.specimenDefinition.preservationTemperature,
        specimenType            = cmd.specimenDefinition.specimenType)
      specimenDefinition = validSpecimenDefinition.copy(id = processingType.output.specimenDefinition.id)
      output <- studies.OutputSpecimenProcessing.create(
        expectedChange     = cmd.expectedChange,
        count              = cmd.count,
        containerTypeId    = cmd.containerTypeId.map(ContainerTypeId.apply),
        specimenDefinition = specimenDefinition)
      updatedProcessingType <- processingType.withOutputSpecimenProcessing(output)
    } yield {
      val time = OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

      // MAKE SURE THE SPECIMEN DESCRIPTION ID IS UNCHANGED
      val outputEvent = ProcessingTypeEvent.OutputSpecimenProcessing().update(
          _.expectedChange          := cmd.expectedChange.toDouble,
          _.count                   := cmd.count,
          _.optionalContainerTypeId := cmd.containerTypeId,
          _.specimenDefinition      := specimenDefinitionToEvent(specimenDefinition))

      ProcessingTypeEvent(processingType.id.id).update(
        _.studyId                                                  := processingType.studyId.id,
        _.sessionUserId                                            := cmd.sessionUserId,
        _.time                                                     := time,
        _.outputSpecimenProcessingUpdated.version                  := cmd.expectedVersion,
        _.outputSpecimenProcessingUpdated.outputSpecimenProcessing := outputEvent)
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
    if (!checkNotInUse(processingType)) {
      EntityInUse(s"processing type in use: ${processingType.id}").failureNel[ProcessingTypeEvent]
    } else {
      ProcessingTypeEvent(processingType.id.id).update(
        _.studyId               := processingType.studyId.id,
        _.sessionUserId         := cmd.sessionUserId,
        _.time                  := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        _.removed.version       := cmd.expectedVersion).successNel[String]
    }
  }

  private def applyAddedEvent(event: ProcessingTypeEvent): Unit = {
    if (!event.eventType.isAdded) {
      log.error(s"invalid event type: $event")
    } else {
      val addedEvent = event.getAdded

      val input =
        if (addedEvent.specimenDefinitionType.isCollected) {
          val collectedEvent = addedEvent.getCollected
          studies.InputSpecimenProcessing(
            expectedChange       = collectedEvent.getExpectedChange,
            count                = collectedEvent.getCount,
            containerTypeId      = collectedEvent.containerTypeId.map(ContainerTypeId.apply),
            definitionType       = collectedDefinition,
            entityId             = CollectionEventTypeId(collectedEvent.getEntityId),
            specimenDefinitionId = SpecimenDefinitionId(collectedEvent.getSpecimenDefinitionId))
        } else {
          val processedEvent = addedEvent.getProcessed
          studies.InputSpecimenProcessing(
            expectedChange       = processedEvent.getExpectedChange,
            count                = processedEvent.getCount,
            containerTypeId      = processedEvent.containerTypeId.map(ContainerTypeId.apply),
            definitionType       = processedDefinition,
            entityId             = ProcessingTypeId(processedEvent.getEntityId),
            specimenDefinitionId = SpecimenDefinitionId(processedEvent.getSpecimenDefinitionId))
        }

      val outputEvent = addedEvent.getOutputSpecimenProcessing
      val output = studies.OutputSpecimenProcessing(
          expectedChange     = outputEvent.getExpectedChange,
          count              = outputEvent.getCount,
          containerTypeId    = outputEvent.containerTypeId.map(ContainerTypeId.apply),
          specimenDefinition = specimenDefinitionFromEvent(outputEvent.getSpecimenDefinition))

      val v = ProcessingType.create(
          studyId         = StudyId(event.getStudyId),
          id              = ProcessingTypeId(event.id),
          version         = 0L,
          name            = addedEvent.getName,
          description     = addedEvent.description,
          enabled         = addedEvent.getEnabled,
          input           = input,
          output          = output,
          annotationTypes = Set.empty)

      if (v.isFailure) {
        log.error(s"could not add processing type from event: $v")
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

  private def applyInputSpecimenProcessingUpdatedEvent(event: ProcessingTypeEvent): Unit = {
    onValidEventAndVersion(
      event,
      event.eventType.isInputSpecimenProcessingUpdated,
      event.getInputSpecimenProcessingUpdated.getVersion
    ) { (processingType, eventTime) =>

      val inputEvent = event.getInputSpecimenProcessingUpdated
      val v = for {
          input <- {
            if (inputEvent.specimenDefinitionType.isCollected) {
              val collectedEvent = event.getInputSpecimenProcessingUpdated.getCollected
              studies.InputSpecimenProcessing.create(
                expectedChange       = collectedEvent.getExpectedChange,
                count                = collectedEvent.getCount,
                containerTypeId      = collectedEvent.containerTypeId.map(ContainerTypeId.apply),
                definitionType       = collectedEvent.getDefinitionType,
                entityId             = collectedEvent.getEntityId,
                specimenDefinitionId = collectedEvent.getSpecimenDefinitionId)
            } else {
              val processedEvent = event.getInputSpecimenProcessingUpdated.getProcessed
              studies.InputSpecimenProcessing.create(
                expectedChange       = processedEvent.getExpectedChange,
                count                = processedEvent.getCount,
                containerTypeId      = processedEvent.containerTypeId.map(ContainerTypeId.apply),
                definitionType       = processedEvent.getDefinitionType,
                entityId             = processedEvent.getEntityId,
                specimenDefinitionId = processedEvent.getSpecimenDefinitionId)
            }
          }
          updated <- processingType.withInputSpecimenProcessing(input)
        } yield updated
      v.foreach { pt =>
        processingTypeRepository.put(pt.copy(timeModified = Some(eventTime)))
      }
      v.map(_ => true)
    }
  }

  private def applyOutputSpecimenProcessingUpdatedEvent(event: ProcessingTypeEvent): Unit = {
    onValidEventAndVersion(
      event,
      event.eventType.isOutputSpecimenProcessingUpdated,
      event.getOutputSpecimenProcessingUpdated.getVersion
    ) { (processingType, eventTime) =>
      val outputEvent = event.getOutputSpecimenProcessingUpdated.getOutputSpecimenProcessing

      val output = studies.OutputSpecimenProcessing(
        expectedChange       = outputEvent.getExpectedChange,
        count                = outputEvent.getCount,
        containerTypeId      = outputEvent.containerTypeId.map(ContainerTypeId.apply),
        specimenDefinition   = specimenDefinitionFromEvent(outputEvent.getSpecimenDefinition))

      val v = processingType.withOutputSpecimenProcessing(output)
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

  def checkNotInUse(processingType: ProcessingType): Boolean = {
    !processingTypeRepository.processingTypeInUse(processingType)

    // FIXME: also check if there are specimens of this type in specimenReporitory
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

  private def validateInputSpecimenProcessing(input: studies.InputSpecimenProcessing)
      : ServiceValidation[Boolean] = {
    val entityIdString = input.entityId.toString
    if (input.definitionType == collectedDefinition) {
      collectionEventTypeRepository.getByKey(CollectionEventTypeId(entityIdString)).flatMap { eventType =>
        eventType.specimenDefinition(input.specimenDefinitionId).map { _ => true }
      }
    } else {
      processingTypeRepository.getByKey(ProcessingTypeId(entityIdString)).flatMap { processingType =>
        if (processingType.output.specimenDefinition.id == input.specimenDefinitionId) {
          true.successNel[String]
        } else {
          ServiceError(s"IdNotFound: specimen definition id: ${input.specimenDefinitionId}")
            .failureNel[Boolean]
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
