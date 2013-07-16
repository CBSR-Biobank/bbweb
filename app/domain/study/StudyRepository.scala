package domain.study

import domain._

import scalaz._
import Scalaz._

object StudyRepository extends ReadWriteRepository[StudyId, Study](v => v.id) {

  def allStudies(studyId: StudyId): Set[Study] = {
    getValues.toSet
  }

  def studyWithId(studyId: StudyId): DomainValidation[Study] = {
    getByKey(studyId) match {
      case Failure(x) => DomainError("study does not exist").fail
      case Success(study) => study.success
    }
  }

  private def nameAvailable(study: DisabledStudy): DomainValidation[Boolean] = {
    val exists = getValues.exists { item =>
      item.id.equals(study.id) && item.name.equals(study.name) && !item.id.equals(study.id)
    }

    if (exists)
      DomainError("study with name already exists: %s" format study.name).fail
    else
      true.success
  }

  def add(study: DisabledStudy): DomainValidation[DisabledStudy] = {
    getByKey(study.id) match {
      case Success(prevItem) =>
        DomainError("study with ID already exists: %s" format study.id).fail
      case Failure(x) =>
        for {
          nameValid <- nameAvailable(study)
          item <- updateMap(study).success
        } yield study
    }
  }

  def update(study: DisabledStudy): DomainValidation[DisabledStudy] = {
    for {
      prevStudy <- studyWithId(study.id)
      validVersion <- prevStudy.requireVersion(Some(study.version))
      nameValid <- nameAvailable(study)
      updatedItem <- updateMap(study).success
    } yield study
  }

  def enable(
    studyId: StudyId,
    specimenGroupCount: Int,
    collectionEventTypecount: Int): DomainValidation[EnabledStudy] = {

    def doEnable(prevStudy: Study) = {
      prevStudy match {
        case es: EnabledStudy =>
          DomainError("study is already enabled: {id: %s}".format(es.id)).fail
        case ds: DisabledStudy =>
          if ((specimenGroupCount == 0) || (collectionEventTypecount == 0))
            DomainError("study has no specimen groups and / or no collection event types").fail
          else {
            val study = EnabledStudy(ds.id, ds.version + 1, ds.name, ds.description)
            updateMap(study)
            study.success
          }
      }
    }

    for {
      prevStudy <- studyWithId(studyId)
      enabledStudy <- doEnable(prevStudy)
    } yield enabledStudy
  }

  def disable(studyId: StudyId): DomainValidation[DisabledStudy] = {

    def doDisable(prevStudy: Study) = {
      prevStudy match {
        case ds: DisabledStudy =>
          DomainError("study is already disabled: {id: %s}".format(ds.id)).fail
        case es: EnabledStudy =>
          val study = DisabledStudy(es.id, es.version + 1, es.name, es.description)
          updateMap(study)
          study.success
      }
    }

    for {
      prevStudy <- studyWithId(studyId)
      disabledStudy <- doDisable(prevStudy)
    } yield disabledStudy
  }

}