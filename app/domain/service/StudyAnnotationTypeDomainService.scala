package domain.service

import org.eligosource.eventsourced.core._

import StudyValidationUtil._
import domain._
import domain.study.{
  CollectionEventAnnotationType,
  CollectionEventType,
  CollectionEventTypeId,
  DisabledStudy,
  EnabledStudy,
  Study,
  StudyAnnotationType,
  StudyId
}
import infrastructure._
import infrastructure.commands._
import infrastructure.events._
import scalaz._
import Scalaz._

/**
 * This domain service class handled commands that deal with study
 * annotation types.
 *
 * @author Nelson Loyola
 */
class StudyAnnotationTypeDomainService(
  annotationTypeRepo: ReadWriteRepository[AnnotationTypeId, StudyAnnotationType],
  annotationOptionRepo: ReadWriteRepository[String, AnnotationOption])
  extends DomainService {

  /** @inheritdoc */
  def process = PartialFunction[Any, DomainValidation[_]] {

    // collection event  annotations
    case _@ (cmd: AddCollectionEventAnnotationTypeCmd, study: DisabledStudy, listeners: MessageEmitter) =>
      addCollectionEventAnnotationType(cmd, study, listeners)
    case _@ (cmd: UpdateCollectionEventAnnotationTypeCmd, study: DisabledStudy, listeners: MessageEmitter) =>
      updateCollectionEventAnnotationType(cmd, study, listeners)
    case _@ (cmd: RemoveCollectionEventAnnotationTypeCmd, study: DisabledStudy, listeners: MessageEmitter) =>
      removeCollectionEventAnnotationType(cmd, study, listeners)

    // annotation options
    case _@ (cmd: AddAnnotationOptionsCmd, study: DisabledStudy, listeners: MessageEmitter) =>
      addAnnotationOptions(cmd, study, listeners)
    case _@ (cmd: UpdateAnnotationOptionsCmd, study: DisabledStudy, listeners: MessageEmitter) =>
      updateAnnotationOptions(cmd, study, listeners)
    case _@ (cmd: RemoveAnnotationOptionsCmd, study: DisabledStudy, listeners: MessageEmitter) =>
      removeAnnotationOptions(cmd, study, listeners)

    case _ =>
      throw new Error("invalid command received")

  }

  /** test */
  private def addCollectionEventAnnotationType(
    cmd: AddCollectionEventAnnotationTypeCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[CollectionEventAnnotationType] = {
    val annotationTypes = annotationTypeRepo.getMap.filter(
      cet => cet._2.studyId.equals(study.id))
    val v = study.addCollectionEventAnnotationType(annotationTypes, cmd)
    v match {
      case Success(annot) =>
        annotationTypeRepo.updateMap(annot)
        listeners sendEvent CollectionEventAnnotationTypeAddedEvent(
          cmd.studyId, annot.name, annot.description, annot.valueType, annot.maxValueCount)
      case _ => // nothing to do in this case
    }
    v
  }

  private def updateCollectionEventAnnotationType(
    cmd: UpdateCollectionEventAnnotationTypeCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[CollectionEventAnnotationType] = {
    val annotationTypeId = new AnnotationTypeId(cmd.annotationTypeId)

    def update(prevAnnot: CollectionEventAnnotationType): DomainValidation[CollectionEventAnnotationType] = {
      val annot = CollectionEventAnnotationType(annotationTypeId, prevAnnot.version + 1, study.id,
        cmd.name, cmd.description, cmd.valueType, cmd.maxValueCount)
      annotationTypeRepo.updateMap(annot)
      listeners sendEvent CollectionEventAnnotationTypeUpdatedEvent(
        cmd.studyId, cmd.annotationTypeId, annot.name, annot.description, annot.valueType,
        annot.maxValueCount)
      annot.success
    }

    for {
      annot <- validateCollectionEventAnnotationTypeId(study, annotationTypeRepo, cmd.annotationTypeId)
      updatedAnnot <- update(annot)
    } yield updatedAnnot
  }

  private def removeCollectionEventAnnotationType(
    cmd: RemoveCollectionEventAnnotationTypeCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[CollectionEventAnnotationType] = {

    def removeItem(annotType: CollectionEventAnnotationType): DomainValidation[CollectionEventAnnotationType] = {
      annotationTypeRepo.remove(annotType)
      listeners sendEvent CollectionEventAnnotationTypeRemovedEvent(cmd.studyId, cmd.annotationTypeId)
      annotType.success
    }

    for {
      annot <- validateCollectionEventAnnotationTypeId(study, annotationTypeRepo, cmd.annotationTypeId)
      removedAnot <- removeItem(annot)
    } yield removedAnot
  }

  private def addAnnotationOptions(
    cmd: AddAnnotationOptionsCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[AnnotationOption] = {

    //annotationOptionRepo

    ???
  }

  private def updateAnnotationOptions(
    cmd: UpdateAnnotationOptionsCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[AnnotationOption] = {
    ???
  }

  private def removeAnnotationOptions(
    cmd: RemoveAnnotationOptionsCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[AnnotationOption] = {
    ???
  }

}