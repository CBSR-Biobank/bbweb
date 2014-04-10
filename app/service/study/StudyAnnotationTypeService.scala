package service.study

import infrastructure.command.StudyCommands._
import infrastructure.event._
import service._
import domain._
import domain.study._
import domain.study.Study._
import domain.AnnotationValueType._

import org.slf4j.LoggerFactory

import scalaz._
import scalaz.Scalaz._

/**
 * This domain service class handled commands that deal with study
 * annotation types.
 *
 * @author Nelson Loyola
 */
abstract class StudyAnnotationTypeService[A <: StudyAnnotationType] extends CommandHandler {

  protected val log = LoggerFactory.getLogger(this.getClass)

  protected def createNewAnnotationType(cmd: StudyAnnotationTypeCommand, id: AnnotationTypeId): A

  protected def createUpdatedAnnotationType(oldAnnotationType: A, cmd: StudyAnnotationTypeCommand): A

  protected def createRemovalAnnotationType(oldAnnotationType: A, cmd: StudyAnnotationTypeCommand): A

  protected def checkNotInUse(annotationType: A): DomainValidation[Boolean]

  protected def addAnnotationType(
    repository: StudyAnnotationTypeRepository[A],
    cmd: StudyAnnotationTypeCommand,
    study: DisabledStudy): DomainValidation[A] = {
    val atId = repository.nextIdentity
    for {
      newItem <- repository.add(createNewAnnotationType(cmd, atId))
    } yield newItem
  }

  protected def updateAnnotationType(
    repository: StudyAnnotationTypeRepository[A],
    cmd: StudyAnnotationTypeCommand,
    annotationTypeId: AnnotationTypeId,
    study: DisabledStudy): DomainValidation[A] = {

    for {
      oldAnnotationType <- repository.annotationTypeWithId(study.id, annotationTypeId)
      notInUse <- checkNotInUse(oldAnnotationType)
      newItem <- repository.update(oldAnnotationType, createUpdatedAnnotationType(oldAnnotationType, cmd))
    } yield newItem
  }

  protected def removeAnnotationType(
    repository: StudyAnnotationTypeRepository[A],
    cmd: StudyAnnotationTypeCommand,
    annotationTypeId: AnnotationTypeId,
    study: DisabledStudy): DomainValidation[A] = {
    for {
      oldItem <- repository.annotationTypeWithId(study.id, annotationTypeId)
      notInUse <- checkNotInUse(oldItem)
      itemToRemove <- createRemovalAnnotationType(oldItem, cmd).success
      removedItem <- repository.remove(itemToRemove)
    } yield removedItem
  }
}
