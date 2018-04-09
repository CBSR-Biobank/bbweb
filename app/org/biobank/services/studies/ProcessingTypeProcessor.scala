package org.biobank.services.studies

import akka.actor._
import akka.persistence.{RecoveryCompleted, SaveSnapshotSuccess, SaveSnapshotFailure, SnapshotOffer}
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import org.biobank.domain._
import org.biobank.domain.containers.ContainerTypeId
import org.biobank.domain.studies._
import org.biobank.infrastructure.commands.ProcessingTypeCommands._
import org.biobank.services.{Processor, ServiceValidation, SnapshotWriter}
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
        case et: EventType.Added   => applyAddedEvent(event)
        // case et: EventType.ProcessingTypeUpdated => applyProcessingTypeUpdatedEvent(event)
        case et: EventType.Removed => applyRemovedEvent(event)

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
    case cmd: AddCollectedProcessingTypeCmd =>
      process(addCmdToEvent(cmd))(applyAddedEvent)

    // case cmd: UpdateProcessingTypeCmd => processUpdateProcessingTypeCmd(cmd)
    case cmd: RemoveProcessingTypeCmd =>
      processUpdateCmd(cmd, removeCmdToEvent, applyRemovedEvent)

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
    val fileContents = snapshotWriter.load(filename);
    Json.parse(fileContents).validate[SnapshotState].fold(
      errors => log.error(s"could not apply snapshot: $filename: $errors"),
      snapshot =>  {
        log.debug(s"snapshot contains ${snapshot.processingTypes.size} processing types")
        snapshot.processingTypes.foreach(processingTypeRepository.put)
      }
    )
  }

  private def addCmdToEvent(cmd: AddCollectedProcessingTypeCmd): ServiceValidation[ProcessingTypeEvent] = {
    val studyId = StudyId(cmd.studyId)
    for {
      ptId      <- validNewIdentity(processingTypeRepository.nextIdentity, processingTypeRepository)
      eventType <- collectionEventTypeRepository.getByKey(CollectionEventTypeId(cmd.collectionEventTypeId))
      nameValid <- nameAvailable(cmd.name, studyId)
      outputSpecimenDef <- {
        ProcessingSpecimenDefinition.create(
          name                    = cmd.outputSpecimenDefinition.name,
          description             = cmd.outputSpecimenDefinition.description,
          units                   = cmd.outputSpecimenDefinition.units,
          anatomicalSourceType    = cmd.outputSpecimenDefinition.anatomicalSourceType,
          preservationType        = cmd.outputSpecimenDefinition.preservationType,
          preservationTemperature = cmd.outputSpecimenDefinition.preservationTemperature,
          specimenType            = cmd.outputSpecimenDefinition.specimenType)
      }
      newItem   <- {
        val specimenDerivation =
          CollectedSpecimenDerivation(eventType.id,
                                      SpecimenDefinitionId(cmd.inputSpecimenDefinitionId),
                                      outputSpecimenDef)
        ProcessingType.create(studyId               = StudyId(cmd.studyId),
                              id                    = ptId,
                              version               = 0L,
                              name                  = cmd.name,
                              description           = cmd.description,
                              enabled               = cmd.enabled,
                              expectedInputChange   = cmd.expectedInputChange,
                              expectedOutputChange  = cmd.expectedOutputChange,
                              inputCount            = cmd.inputCount,
                              outputCount           = cmd.outputCount,
                              inputContainerTypeId  = cmd.inputContainerTypeId.map(ContainerTypeId.apply),
                              outputContainerTypeId = cmd.outputContainerTypeId.map(ContainerTypeId.apply),
                              annotationTypes       = Set.empty,
                              specimenDerivation    = specimenDerivation)
      }
    } yield {
      val timeStr = OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

      ProcessingTypeEvent(ptId.id).update(
        _.studyId                                   := studyId.id,
        _.sessionUserId                             := cmd.sessionUserId,
        _.time                                      := timeStr,
        _.added.name                                := newItem.name,
        _.added.optionalDescription                 := newItem.description,
        _.added.enabled                             := newItem.enabled,
        _.added.expectedInputChange                 := newItem.expectedInputChange.toDouble,
        _.added.expectedOutputChange                := newItem.expectedOutputChange.toDouble,
        _.added.inputCount                          := newItem.inputCount,
        _.added.outputCount                         := newItem.outputCount,
        _.added.optionalInputContainerTypeId        := cmd.inputContainerTypeId,
        _.added.optionalOutputContainerTypeId       := cmd.outputContainerTypeId,
        _.added.collected.inputEntityId             := cmd.collectionEventTypeId,
        _.added.collected.inputSpecimenDefinitionId := cmd.inputSpecimenDefinitionId,
        _.added.collected.outputSpecimenDefinition  := specimenDefinitionToEvent(outputSpecimenDef))

    }

  }

  // private def processUpdateProcessingTypeCmd(cmd: UpdateProcessingTypeCmd): Unit = {
  //   val v = update(cmd) { pt =>
  //     for {
  //       nameValid <- nameAvailable(cmd.name, StudyId(cmd.studyId), ProcessingTypeId(cmd.id))
  //       updatedPt <- pt.update(cmd.name, cmd.description, cmd.enabled)
  //       event <- createStudyEvent(updatedPt.studyId, cmd).withProcessingTypeUpdated(
  //         ProcessingTypeUpdatedEvent(
  //           processingTypeId = Some(updatedPt.id.id),
  //           version          = Some(updatedPt.version),
  //           name             = Some(updatedPt.name),
  //           description      = updatedPt.description,
  //           enabled          = Some(updatedPt.enabled))).successNel[String]
  //     } yield event
  //   }
  //   process(v) { applyProcessingTypeUpdatedEvent(_) }
  // }

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
      val collected = addedEvent.getCollected
      val specimenDerivation = CollectedSpecimenDerivation(
          CollectionEventTypeId(collected.getInputEntityId),
          SpecimenDefinitionId(collected.getInputSpecimenDefinitionId),
          specimenDefinitionFromEvent(collected.getOutputSpecimenDefinition))

      val v = ProcessingType.create(
          studyId               = StudyId(event.getStudyId),
          id                    = ProcessingTypeId(event.id),
          version               = 0L,
          name                  = addedEvent.getName,
          description           = addedEvent.description,
          enabled               = addedEvent.getEnabled,
          expectedInputChange   = addedEvent.getExpectedInputChange,
          expectedOutputChange  = addedEvent.getExpectedOutputChange,
          inputCount            = addedEvent.getInputCount,
          outputCount           = addedEvent.getOutputCount,
          specimenDerivation    = specimenDerivation,
          inputContainerTypeId  = inputContainerTypeId,
          outputContainerTypeId = outputContainerTypeId,
          annotationTypes       = Set.empty)

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

  // private def applyProcessingTypeAddedEvent(event: StudyEventOld): Unit = {
  //   if (event.eventType.isProcessingTypeAdded) {
  //     val addedEvent = event.getProcessingTypeAdded

  //     val pt = ProcessingType.create(studyId      = StudyId(event.id),
  //                                    id           = ProcessingTypeId(addedEvent.getProcessingTypeId),
  //                                    version      = 0L,
  //                                    name         = addedEvent.getName,
  //                                    description  = addedEvent.description,
  //                                    enabled      = addedEvent.getEnabled)

  //     pt.foreach(processingTypeRepository.put)
  //   } else {
  //     log.error(s"invalid event type: $event")
  //   }
  // }

  // private def applyProcessingTypeUpdatedEvent(event: StudyEventOld): Unit = {
  //   if (event.eventType.isProcessingTypeUpdated) {
  //     val updatedEvent = event.getProcessingTypeUpdated

  //     val v = processingTypeRepository.getByKey(ProcessingTypeId(updatedEvent.getProcessingTypeId))

  //     if (v.isFailure) {
  //       log.error(s"updating processing type from event failed: $v")
  //     }

  //     v.foreach(pt => processingTypeRepository.put(
  //                 pt.copy(version      = updatedEvent.getVersion,
  //                         timeModified  = Some(OffsetDateTime.parse(event.getTime)),
  //                         name         = updatedEvent.getName,
  //                         description  = updatedEvent.description,
  //                         enabled      = updatedEvent.getEnabled)))

  //   } else {
  //     log.error(s"invalid event type: $event")
  //   }
  // }

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

  // @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  // private def nameAvailable(name: String, studyId: StudyId): ServiceValidation[Boolean] = {
  //   nameAvailableMatcher(name, processingTypeRepository, ErrMsgNameExists){ item =>
  //     (item.name == name) && (item.studyId == studyId)
  //   }
  // }

  // @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  // private def nameAvailable(name: String,
  //                           studyId: StudyId,
  //                           excludeId: ProcessingTypeId): ServiceValidation[Boolean] = {
  //   nameAvailableMatcher(name, processingTypeRepository, ErrMsgNameExists){ item =>
  //     (item.name == name) && (item.studyId == studyId) && (item.id != excludeId)
  //   }
  // }

  def checkNotInUse(processingType: ProcessingType): ServiceValidation[ProcessingType] = {
    // FIXME: this is a stub for now
    //
    // it needs to be replaced with the real check
    processingType.successNel[String]
  }

  private def processUpdateCmd[T <: ProcessingTypeModifyCommand](
    cmd: T,
    cmdToEvent: (T, ProcessingType) => ServiceValidation[ProcessingTypeEvent],
    applyEvent: ProcessingTypeEvent => Unit
  ): Unit = {
    val event = for {
        cet          <- processingTypeRepository.withId(StudyId(cmd.studyId),
                                                        ProcessingTypeId(cmd.id))
        validVersion <- cet.requireVersion(cmd.expectedVersion)
        event        <- cmdToEvent(cmd, cet)
      } yield event

    process(event)(applyEvent)
  }

  private def nameAvailable(name: String, studyId: StudyId): ServiceValidation[Boolean] = {
    nameAvailableMatcher(name, processingTypeRepository, ErrMsgNameExists) { item =>
      (item.name == name) && (item.studyId == studyId)
    }
  }

  // @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  // private def nameAvailable(name: String,
  //                           studyId: StudyId,
  //                           excludeId: ProcessingTypeId)
  //     : ServiceValidation[Boolean] = {
  //   nameAvailableMatcher(name, processingTypeRepository, ErrMsgNameExists){ item =>
  //     (item.name == name) && (item.studyId == studyId) && (item.id != excludeId)
  //   }
  // }

  private def init(): Unit = {
    processingTypeRepository.init
  }

  def specimenDefinitionToEvent(specimenDesc: ProcessingSpecimenDefinition):
      ProcessingTypeEvent.SpecimenDefinition = {
    ProcessingTypeEvent.SpecimenDefinition().update(
      _.id                      := specimenDesc.id.id,
      _.name                    := specimenDesc.name,
      _.optionalDescription     := specimenDesc.description,
      _.units                   := specimenDesc.units,
      _.anatomicalSourceType    := specimenDesc.anatomicalSourceType.toString,
      _.preservationType        := specimenDesc.preservationType.toString,
      _.preservationTemperature := specimenDesc.preservationTemperature.toString,
      _.specimenType            := specimenDesc.specimenType.toString
    )
  }

  def specimenDefinitionFromEvent(event: ProcessingTypeEvent.SpecimenDefinition)
      : ProcessingSpecimenDefinition = {
    ProcessingSpecimenDefinition(
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


  init
}
