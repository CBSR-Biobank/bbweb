package service

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.stm.Ref
import scala.language.postfixOps
import org.eligosource.eventsourced.core._
import domain.{
  AnnotationOption,
  AnnotationTypeId,
  ConcurrencySafeEntity,
  DomainValidation,
  DomainError,
  Entity
}
import domain.study._
import infrastructure.commands._
import infrastructure.events._
import infrastructure._
import domain.service.{
  CollectionEventTypeDomainService,
  SpecimenGroupDomainService,
  StudyAnnotationTypeDomainService,
  StudyValidationUtil
}
import StudyValidationUtil._
import scalaz._
import Scalaz._

/**
 * This is the Study Aggregate Processor.
 *
 * It handles the commands to configure studies.
 *
 * @param studyRepository The repository for study entities.
 * @param specimenGroupRepository The repository for specimen group entities.
 * @param cetRepo The repository for Container Event Type entities.
 * @param annotationTypeRepo The repository for Collection Event Annotation Type entities.
 * @param sg2cetRepo The value object repository that associates a specimen group to a
 *         collection event type.
 * @param at2cetRepo The value object repository that associates a collection event annotation
 *         type to a collection event type.
 */
class StudyProcessor(
  studyRepository: ReadWriteRepository[StudyId, Study],
  specimenGroupRepository: ReadWriteRepository[SpecimenGroupId, SpecimenGroup],
  cetRepo: ReadWriteRepository[CollectionEventTypeId, CollectionEventType],
  annotationTypeRepo: ReadWriteRepository[AnnotationTypeId, StudyAnnotationType],
  annotationOptionRepo: ReadWriteRepository[String, AnnotationOption],
  sg2cetRepo: ReadWriteRepository[String, SpecimenGroupCollectionEventType],
  at2cetRepo: ReadWriteRepository[String, CollectionEventTypeAnnotationType])
  extends Processor { this: Emitter =>

  /**
   * The domain service that handles specimen group commands.
   */
  val specimenGroupDomainService = new SpecimenGroupDomainService(
    studyRepository, specimenGroupRepository)

  /**
   * The domain service that handles collection event type commands.
   */
  val collectionEventTypeDomainService = new CollectionEventTypeDomainService(
    studyRepository, cetRepo, specimenGroupRepository, annotationTypeRepo,
    sg2cetRepo, at2cetRepo)

  val annotationTypeDomainService = new StudyAnnotationTypeDomainService(
    annotationTypeRepo, annotationOptionRepo)

  def receive = {
    case cmd: AddStudyCmd =>
      process(addStudy(cmd, emitter("listeners")))

    case cmd: UpdateStudyCmd =>
      process(updateStudy(cmd, emitter("listeners")))

    case cmd: EnableStudyCmd =>
      process(enableStudy(cmd, emitter("listeners")))

    case cmd: DisableStudyCmd =>
      process(disableStudy(cmd, emitter("listeners")))

    case cmd: SpecimenGroupCommand =>
      process(validateStudy(cmd.studyId, studyRepository) { study =>
        specimenGroupDomainService.process(cmd, study, emitter("listeners"))
      })

    case cmd: CollectionEventTypeCommand =>
      process(validateStudy(cmd.studyId, studyRepository) { study =>
        collectionEventTypeDomainService.process(cmd, study, emitter("listeners"))
      })

    case cmd: StudyAnnotationTypeCommand =>
      process(validateStudy(cmd.studyId, studyRepository) { study =>
        annotationTypeDomainService.process(cmd, study, emitter("listeners"))
      })

    case _ =>
      throw new Error("invalid command received")
  }

  private def addStudy(cmd: AddStudyCmd, listeners: MessageEmitter): DomainValidation[DisabledStudy] = {
    studyRepository.getValues.find(s => s.name.equals(cmd.name)) match {
      case Some(study) =>
        DomainError("study with name already exists: %s" format cmd.name).fail
      case None =>
        val study = Study.add(cmd.name, cmd.description)
        study match {
          case Success(study) =>
            studyRepository.updateMap(study)
            listeners sendEvent StudyAddedEvent(study.id, study.name, study.description)
          case _ => // nothing to do in this case
        }
        study
    }
  }

  private def updateStudy(cmd: UpdateStudyCmd, listeners: MessageEmitter): DomainValidation[DisabledStudy] = {
    val studyId = new StudyId(cmd.studyId)
    Entity.update(studyRepository.getByKey(studyId), studyId, cmd.expectedVersion) { prevStudy =>
      val study = DisabledStudy(studyId, prevStudy.version + 1, cmd.name, cmd.description)
      studyRepository.updateMap(study)
      listeners sendEvent StudyUpdatedEvent(study.id, study.name, study.description)
      study.success
    }
  }

  private def enableStudy(cmd: EnableStudyCmd, listeners: MessageEmitter): DomainValidation[EnabledStudy] = {
    val studyId = new StudyId(cmd.studyId)
    Entity.update(studyRepository.getByKey(studyId), studyId, cmd.expectedVersion) { prevStudy =>
      val study = EnabledStudy(studyId, prevStudy.version + 1, prevStudy.name, prevStudy.description)
      studyRepository.updateMap(study)
      listeners sendEvent StudyEnabledEvent(study.id)
      study.success
    }
  }

  private def disableStudy(cmd: DisableStudyCmd, listeners: MessageEmitter): DomainValidation[DisabledStudy] = {
    val studyId = new StudyId(cmd.studyId)
    Entity.update(studyRepository.getByKey(studyId), studyId, cmd.expectedVersion) { prevStudy =>
      val study = DisabledStudy(studyId, prevStudy.version + 1, prevStudy.name, prevStudy.description)
      studyRepository.updateMap(study)
      listeners sendEvent StudyDisabledEvent(study.id)
      study.success
    }
  }
}
