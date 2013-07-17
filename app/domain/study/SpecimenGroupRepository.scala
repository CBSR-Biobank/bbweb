package domain.study

import domain._

import scalaz._
import Scalaz._

object SpecimenGroupRepository
  extends ReadWriteRepository[SpecimenGroupId, SpecimenGroup](v => v.id) {

  def specimenGroupWithId(
    studyId: StudyId,
    specimenGroupId: SpecimenGroupId): DomainValidation[SpecimenGroup] = {
    getByKey(specimenGroupId) match {
      case Failure(x) => DomainError("specimen group does not exist").fail
      case Success(sg) =>
        if (sg.studyId.equals(studyId)) sg.success
        else DomainError("study does not have specimen group").fail
    }

  }
  def allSpecimenGroupsForStudy(studyId: StudyId): Set[SpecimenGroup] = {
    getValues.filter(x => x.studyId.equals(id)).toSet
  }

  private def nameAvailable(specimenGroup: SpecimenGroup): DomainValidation[Boolean] = {
    val exists = getValues.exists { item =>
      item.studyId.equals(specimenGroup.studyId) &&
        item.name.equals(specimenGroup.name) &&
        !item.id.equals(specimenGroup.id)
    }

    if (exists)
      DomainError("specimen group with name already exists: %s" format specimenGroup.name).fail
    else
      true.success
  }

  def add(specimenGroup: SpecimenGroup): DomainValidation[SpecimenGroup] = {
    specimenGroupWithId(specimenGroup.studyId, specimenGroup.id) match {
      case Success(prevItem) =>
        DomainError("specimen group with ID already exists: %s" format specimenGroup.id).fail
      case Failure(x) =>
        for {
          nameValid <- nameAvailable(specimenGroup)
          item <- updateMap(specimenGroup).success
        } yield item
    }
  }

  def update(specimenGroup: SpecimenGroup): DomainValidation[SpecimenGroup] = {
    for {
      prevItem <- specimenGroupWithId(specimenGroup.studyId, specimenGroup.id)
      validVersion <- prevItem.requireVersion(Some(specimenGroup.version))
      nameValid <- nameAvailable(specimenGroup)
      updatedItem <- updateMap(specimenGroup).success
    } yield updatedItem
  }

  def remove(specimenGroup: SpecimenGroup): DomainValidation[SpecimenGroup] = {
    for {
      item <- specimenGroupWithId(specimenGroup.studyId, specimenGroup.id)
      validVersion <- item.requireVersion(Some(specimenGroup.version))
      removedItem <- removeFromMap(item).success
    } yield removedItem

  }

}