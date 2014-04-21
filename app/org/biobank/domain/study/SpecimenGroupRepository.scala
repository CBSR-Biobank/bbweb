package org.biobank.domain.study

import org.biobank.domain._

import org.slf4j.LoggerFactory

import scalaz._
import Scalaz._

trait SpecimenGroupRepositoryComponent {

  val specimenGroupRepository: SpecimenGroupRepository

  trait SpecimenGroupRepository extends ReadWriteRepository[SpecimenGroupId, SpecimenGroup] {

    def nextIdentity: SpecimenGroupId

    def allSpecimenGroupsForStudy(studyId: StudyId): Set[SpecimenGroup]

    def specimenGroupWithId(
      studyId: StudyId,
      specimenGroupId: SpecimenGroupId): DomainValidation[SpecimenGroup]

  }
}

trait SpecimenGroupRepositoryComponentImpl extends SpecimenGroupRepositoryComponent {

  override val specimenGroupRepository: SpecimenGroupRepository = new SpecimenGroupRepositoryImpl

  class SpecimenGroupRepositoryImpl
      extends ReadWriteRepositoryRefImpl[SpecimenGroupId, SpecimenGroup](v => v.id)
      with SpecimenGroupRepository {

    val log = LoggerFactory.getLogger(this.getClass)

    def nextIdentity: SpecimenGroupId =
      new SpecimenGroupId(java.util.UUID.randomUUID.toString.toUpperCase)

    def allSpecimenGroupsForStudy(studyId: StudyId): Set[SpecimenGroup] = {
      getValues.filter(x => x.studyId.equals(studyId)).toSet
    }

  def specimenGroupWithId(
    studyId: StudyId,
    specimenGroupId: SpecimenGroupId): DomainValidation[SpecimenGroup] = {
    getByKey(specimenGroupId) match {
      case Failure(err) => DomainError(
	s"specimen group does not exist: { studyId: $studyId, specimenGroupId: $specimenGroupId }")
	  .failNel
      case Success(sg) if (sg.studyId.equals(studyId)) => sg.success
      case _ => DomainError(
        s"study does not have specimen group: { studyId: $studyId, specimenGroupId: $specimenGroupId }")
	  .failNel
    }
  }
  }
}
