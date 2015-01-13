package org.biobank.service.study

import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.service.{ Processor, WrappedCommand, WrappedEvent }
import org.biobank.domain._
import org.biobank.domain.user.UserId
import org.biobank.domain.study._
import org.biobank.domain.study.Study
import org.biobank.domain.AnnotationValueType._

import akka.persistence.SnapshotOffer
import org.joda.time.DateTime
import scaldi.akka.AkkaInjectable
import scaldi.{Injectable, Injector}
import scalaz._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

/**
  * The SpecimenLinkAnnotationTypeProcessor is responsible for maintaining state changes for all
  * [[org.biobank.domain.study.SpecimenLinkAnnotationType]] aggregates. This particular processor uses
  * Akka-Persistence's [[akka.persistence.PersistentActor]]. It receives Commands and if valid will persist
  * the generated events, afterwhich it will updated the current state of the
  * [[org.biobank.domain.study.SpecimenLinkAnnotationType]] being processed.
  *
  * It is a child actor of [[org.biobank.service.study.StudiesProcessorComponent.StudiesProcessor]].
  */
class SpecimenLinkAnnotationTypeProcessor(implicit inj: Injector)
    extends StudyAnnotationTypeProcessor[SpecimenLinkAnnotationType]
    with AkkaInjectable {

  override def persistenceId = "specimen-link-annotation-type-processor-id"

  override val annotationTypeRepository = inject [SpecimenLinkAnnotationTypeRepository]

  val specimenLinkTypeRepository = inject [SpecimenLinkTypeRepository]

  case class SnapshotState(annotationTypes: Set[SpecimenLinkAnnotationType])

  /**
    * These are the events that are recovered during journal recovery. They cannot fail and must be
    * processed to recreate the current state of the aggregate.
    */
  val receiveRecover: Receive = {
    case wevent: WrappedEvent[_] =>
      wevent.event match {
        case event: SpecimenLinkAnnotationTypeAddedEvent =>
          recoverSpecimenLinkAnnotationTypeAddedEvent(event, wevent.userId, wevent.dateTime)
        case event: SpecimenLinkAnnotationTypeUpdatedEvent =>
          recoverSpecimenLinkAnnotationTypeUpdatedEvent(event, wevent.userId, wevent.dateTime)
        case event: SpecimenLinkAnnotationTypeRemovedEvent =>
          recoverSpecimenLinkAnnotationTypeRemovedEvent(event, wevent.userId, wevent.dateTime)

        case event => log.error(s"event not handled: $event")
      }

    case SnapshotOffer(_, snapshot: SnapshotState) =>
      snapshot.annotationTypes.foreach{ annotType => annotationTypeRepository.put(annotType) }
  }

  /**
    * These are the commands that are requested. A command can fail, and will send the failure as a response
    * back to the user. Each valid command generates one or more events and is journaled.
    */
  val receiveCommand: Receive = {
    case procCmd: WrappedCommand =>
      implicit val userId = procCmd.userId
      procCmd.command match {
        case cmd: AddSpecimenLinkAnnotationTypeCmd =>    processAddSpecimenLinkAnnotationTypeCmd(cmd)
        case cmd: UpdateSpecimenLinkAnnotationTypeCmd => processUpdateSpecimenLinkAnnotationTypeCmd(cmd)
        case cmd: RemoveSpecimenLinkAnnotationTypeCmd => processRemoveSpecimenLinkAnnotationTypeCmd(cmd)
      }

    case "snap" =>
      saveSnapshot(SnapshotState(annotationTypeRepository.getValues.toSet))
      stash()

    case cmd => log.error(s"SpecimenLinkAnnotationTypeProcessor: message not handled: $cmd")
  }

  /** Updates to annotation types only allowed if they are not being used by any participants.
    */
  def update
    (cmd: StudyAnnotationTypeModifyCommand)
    (fn: SpecimenLinkAnnotationType => DomainValidation[SpecimenLinkAnnotationType])
      : DomainValidation[SpecimenLinkAnnotationType] = {
    for {
      annotType        <- annotationTypeRepository.withId(StudyId(cmd.studyId), cmd.id)
      notInUse         <- checkNotInUse(annotType)
      validVersion     <- annotType.requireVersion( cmd.expectedVersion)
      updatedAnnotType <- fn(annotType)
    } yield updatedAnnotType
  }

  private def processAddSpecimenLinkAnnotationTypeCmd
    (cmd: AddSpecimenLinkAnnotationTypeCmd)
    (implicit userId: Option[UserId])
      : Unit = {
    val timeNow = DateTime.now
    val id = annotationTypeRepository.nextIdentity
    val event = for {
      nameValid <- nameAvailable(cmd.name)
      newItem <- SpecimenLinkAnnotationType.create(
        StudyId(cmd.studyId), id, -1L, timeNow, cmd.name, cmd.description, cmd.valueType,
        cmd.maxValueCount, cmd.options)
      event <- SpecimenLinkAnnotationTypeAddedEvent(
        studyId          = newItem.studyId.id,
        annotationTypeId = newItem.id.id,
        name             = Some(newItem.name),
        description      = newItem.description,
        valueType        = Some(newItem.valueType.toString),
        maxValueCount    = newItem.maxValueCount,
        options          = newItem.options).success
    } yield event

    process(event){ wevent =>
      recoverSpecimenLinkAnnotationTypeAddedEvent(wevent.event, wevent.userId, wevent.dateTime)
    }
  }


  private def processUpdateSpecimenLinkAnnotationTypeCmd
    (cmd: UpdateSpecimenLinkAnnotationTypeCmd)
    (implicit userId: Option[UserId])
      : Unit = {
    val timeNow = DateTime.now
    val v = update(cmd) { at =>
      for {
        nameAvailable <- nameAvailable(cmd.name, AnnotationTypeId(cmd.id))
        newItem <- at.update(cmd.name, cmd.description, cmd.valueType, cmd.maxValueCount, cmd.options)
      } yield newItem
    }

    val event = v.fold(
      err => DomainError(s"error $err occurred on $cmd").failureNel,
      at => SpecimenLinkAnnotationTypeUpdatedEvent(
        studyId          = at.studyId.id,
        annotationTypeId = at.id.id,
        version          = Some(at.version),
        name             = Some(at.name),
        description      = at.description,
        valueType        = Some(at.valueType.toString),
        maxValueCount    = at.maxValueCount,
        options          = at.options).success
    )
    process(event){ wevent =>
      recoverSpecimenLinkAnnotationTypeUpdatedEvent(wevent.event, wevent.userId, wevent.dateTime)
    }
  }

  private def processRemoveSpecimenLinkAnnotationTypeCmd
    (cmd: RemoveSpecimenLinkAnnotationTypeCmd)
    (implicit userId: Option[UserId])
      : Unit = {
    val v = update(cmd) { at => at.success }

    val event = v.fold(
      err => DomainError(s"error $err occurred on $cmd").failureNel,
      at =>  SpecimenLinkAnnotationTypeRemovedEvent(at.studyId.id, at.id.id).success
    )
    process(event){ wevent =>
      recoverSpecimenLinkAnnotationTypeRemovedEvent(wevent.event, wevent.userId, wevent.dateTime)
    }
  }


  private def recoverSpecimenLinkAnnotationTypeAddedEvent
    (event: SpecimenLinkAnnotationTypeAddedEvent, userId: Option[UserId], dateTime: DateTime)
      : Unit = {
    annotationTypeRepository.put(SpecimenLinkAnnotationType(
      studyId       = StudyId(event.studyId),
      id            = AnnotationTypeId(event.annotationTypeId),
      version       = 0L,
      timeAdded     = dateTime,
      timeModified  = None,
      name          = event.getName,
      description   = event.description,
      valueType     = AnnotationValueType.withName(event.getValueType),
      maxValueCount = event.maxValueCount,
      options       = event.options))
    ()
  }

  private def recoverSpecimenLinkAnnotationTypeUpdatedEvent
    (event: SpecimenLinkAnnotationTypeUpdatedEvent, userId: Option[UserId], dateTime: DateTime)
      : Unit = {
    annotationTypeRepository.getByKey(AnnotationTypeId(event.annotationTypeId)).fold(
      err => log.error(s"updating annotatiotn type from event failed: $err"),
      at => {
        annotationTypeRepository.put(at.copy(
          version       = event.getVersion,
          name          = event.getName,
          description   = event.description,
          valueType     = AnnotationValueType.withName(event.getValueType),
          maxValueCount = event.maxValueCount,
          options       = event.options,
          timeModified  = Some(dateTime)))
        ()
      }
    )
  }

  private def recoverSpecimenLinkAnnotationTypeRemovedEvent
    (event: SpecimenLinkAnnotationTypeRemovedEvent, userId: Option[UserId], dateTime: DateTime)
      : Unit = {
    recoverStudyAnnotationTypeRemovedEvent(AnnotationTypeId(event.annotationTypeId))
  }

  def checkNotInUse(annotationType: SpecimenLinkAnnotationType)
      : DomainValidation[SpecimenLinkAnnotationType] = {
    if (specimenLinkTypeRepository.annotationTypeInUse(annotationType)) {
      DomainError(s"annotation type is in use by specimen link type: ${annotationType.id}").failureNel
    } else {
      annotationType.success
    }
  }

}
