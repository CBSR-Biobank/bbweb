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
        case event: SpecimenLinkAnnotationTypeAddedEvent =>   recoverEvent(event, wevent.userId, wevent.dateTime)
        case event: SpecimenLinkAnnotationTypeUpdatedEvent => recoverEvent(event, wevent.userId, wevent.dateTime)
        case event: SpecimenLinkAnnotationTypeRemovedEvent => recoverEvent(event, wevent.userId, wevent.dateTime)

        case event => throw new IllegalStateException(s"event not handled: $event")
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
        case cmd: AddSpecimenLinkAnnotationTypeCmd =>    process(validateCmd(cmd)){ wevent => recoverEvent(wevent.event, wevent.userId, wevent.dateTime) }
        case cmd: UpdateSpecimenLinkAnnotationTypeCmd => process(validateCmd(cmd)){ wevent => recoverEvent(wevent.event, wevent.userId, wevent.dateTime) }
        case cmd: RemoveSpecimenLinkAnnotationTypeCmd => process(validateCmd(cmd)){ wevent => recoverEvent(wevent.event, wevent.userId, wevent.dateTime) }
      }

    case "snap" =>
      saveSnapshot(SnapshotState(annotationTypeRepository.getValues.toSet))
      stash()

    case cmd => log.error(s"SpecimenLinkAnnotationTypeProcessor: message not handled: $cmd")
  }

  /** Updates to annotation types only allowed if they are not being used by any participants.
    */
  def update
    (cmd: SpecimenLinkAnnotationTypeCommand)
    (fn: SpecimenLinkAnnotationType => DomainValidation[SpecimenLinkAnnotationType])
      : DomainValidation[SpecimenLinkAnnotationType] = {
    for {
      annotType        <- annotationTypeRepository.withId(StudyId(cmd.studyId), cmd.id)
      notInUse         <- checkNotInUse(annotType)
      validVersion     <- annotType.requireVersion( cmd.expectedVersion)
      updatedAnnotType <- fn(annotType)
    } yield updatedAnnotType
  }

  private def validateCmd(cmd: AddSpecimenLinkAnnotationTypeCmd)
      : DomainValidation[SpecimenLinkAnnotationTypeAddedEvent] = {
    val timeNow = DateTime.now
    val id = annotationTypeRepository.nextIdentity
    for {
      nameValid <- nameAvailable(cmd.name)
      newItem <- SpecimenLinkAnnotationType.create(
        StudyId(cmd.studyId), id, -1L, timeNow, cmd.name, cmd.description, cmd.valueType,
        cmd.maxValueCount, cmd.options)
      event <- SpecimenLinkAnnotationTypeAddedEvent(
        newItem.studyId.id, newItem.id.id, newItem.name, newItem.description,
        newItem.valueType, newItem.maxValueCount, newItem.options).success
    } yield event
  }


  private def validateCmd(cmd: UpdateSpecimenLinkAnnotationTypeCmd)
      : DomainValidation[SpecimenLinkAnnotationTypeUpdatedEvent] = {
    val timeNow = DateTime.now
    val v = update(cmd) { at =>
      for {
        nameAvailable <- nameAvailable(cmd.name, AnnotationTypeId(cmd.id))
        newItem <- at.update(cmd.name, cmd.description, cmd.valueType, cmd.maxValueCount, cmd.options)
      } yield newItem
    }

    v.fold(
      err => DomainError(s"error $err occurred on $cmd").failNel,
      at => SpecimenLinkAnnotationTypeUpdatedEvent(
        at.studyId.id, at.id.id, at.version, at.name, at.description, at.valueType,
        at.maxValueCount, at.options).success
    )
  }

  private def validateCmd(cmd: RemoveSpecimenLinkAnnotationTypeCmd)
      : DomainValidation[SpecimenLinkAnnotationTypeRemovedEvent] = {
    val v = update(cmd) { at => at.success }

    v.fold(
      err => DomainError(s"error $err occurred on $cmd").failNel,
      at =>  SpecimenLinkAnnotationTypeRemovedEvent(at.studyId.id, at.id.id).success
    )
  }


  private def recoverEvent(event: SpecimenLinkAnnotationTypeAddedEvent, userId: Option[UserId], dateTime: DateTime): Unit = {
    annotationTypeRepository.put(SpecimenLinkAnnotationType(
      StudyId(event.studyId), AnnotationTypeId(event.annotationTypeId), 0L, dateTime, None,
      event.name, event.description, event.valueType, event.maxValueCount, event.options))
    ()
  }

  private def recoverEvent(event: SpecimenLinkAnnotationTypeUpdatedEvent, userId: Option[UserId], dateTime: DateTime): Unit = {
    annotationTypeRepository.getByKey(AnnotationTypeId(event.annotationTypeId)).fold(
      err => throw new IllegalStateException(s"updating annotatiotn type from event failed: $err"),
      at => annotationTypeRepository.put(at.copy(
        version       = event.version,
        name          = event.name,
        description   = event.description,
        valueType     = event.valueType,
        maxValueCount = event.maxValueCount,
        options       = event.options,
        timeModified = Some(dateTime)))
    )
    ()
  }

  private def recoverEvent(event: SpecimenLinkAnnotationTypeRemovedEvent, userId: Option[UserId], dateTime: DateTime): Unit = {
    recoverEvent(AnnotationTypeId(event.annotationTypeId))
  }

  def checkNotInUse(annotationType: SpecimenLinkAnnotationType)
      : DomainValidation[SpecimenLinkAnnotationType] = {
    if (specimenLinkTypeRepository.annotationTypeInUse(annotationType)) {
      DomainError(s"annotation type is in use by specimen link type: ${annotationType.id}").failNel
    } else {
      annotationType.success
    }
  }

}
