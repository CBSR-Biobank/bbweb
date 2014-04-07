package domain.study

import domain._

import org.slf4j.LoggerFactory

import scalaz._
import Scalaz._

trait StudyRepositoryComponent {

  val studyRepository: StudyRepository

  trait StudyRepository {

    def nextIdentity: StudyId

    def nameAvailable(study: DisabledStudy): DomainValidation[Boolean]

    def allStudies(): Set[Study]

    def studyWithId(studyId: StudyId): DomainValidation[Study]

    def add(study: DisabledStudy): DomainValidation[DisabledStudy]

    def update(study: DisabledStudy): DomainValidation[DisabledStudy]

    def enable(
      studyId: StudyId,
      specimenGroupCount: Int,
      collectionEventTypecount: Int): DomainValidation[EnabledStudy]

    def disable(studyId: StudyId): DomainValidation[DisabledStudy]
  }
}

trait StudyRepositoryComponentImpl extends StudyRepositoryComponent {

  override val studyRepository: StudyRepository = new StudyRepositoryImpl

  class StudyRepositoryImpl extends ReadWriteRepository[StudyId, Study](v => v.id) with StudyRepository {

    val log = LoggerFactory.getLogger(this.getClass)

    def nextIdentity: StudyId = new StudyId(java.util.UUID.randomUUID.toString.toUpperCase)

    def allStudies(): Set[Study] = {
      getValues.toSet
    }

    def studyWithId(studyId: StudyId): DomainValidation[Study] = {
      getByKey(studyId) match {
        case Failure(x) => DomainError("study does not exist: { studyId: %s }".format(studyId)).fail
        case Success(study) => study.success
      }
    }

    def nameAvailable(study: DisabledStudy): DomainValidation[Boolean] = {
      val exists = getValues.exists { item =>
        item.name.equals(study.name) && !item.id.equals(study.id)
      }

      if (exists) {
        DomainError("study with name already exists: %s" format study.name).fail
      } else {
        true.success
      }
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
        updatedItem <- DisabledStudy(
          study.id, prevStudy.version + 1, study.name, study.description).success
        repoItem <- updateMap(updatedItem).success
      } yield updatedItem
    }

    def enable(
      studyId: StudyId,
      specimenGroupCount: Int,
      collectionEventTypecount: Int): DomainValidation[EnabledStudy] = {

      def doEnable(prevStudy: Study) = {
        throw new Error("this functionality should not be here")

        prevStudy match {
          case es: EnabledStudy =>
            DomainError("study is already enabled: {id: %s}".format(es.id)).fail
          case ds: DisabledStudy =>
            if ((specimenGroupCount == 0) || (collectionEventTypecount == 0))
              DomainError("study has no specimen groups and / or no collection event types").fail
            else {
              EnabledStudy(ds.id, ds.version + 1, ds.name, ds.description).success
            }
        }
      }

      log.debug("enableStudy: { sgCount: %d, cetCount: %d }".format(
        specimenGroupCount, collectionEventTypecount))

      for {
        prevStudy <- studyWithId(studyId)
        enabledStudy <- doEnable(prevStudy)
        repoItem <- updateMap(enabledStudy).success
      } yield enabledStudy
    }

    def disable(studyId: StudyId): DomainValidation[DisabledStudy] = {
      throw new Error("this functionality should not be here")

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
}