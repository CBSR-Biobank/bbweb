package domain.study

import domain._

import org.slf4j.LoggerFactory

import scalaz._
import Scalaz._

object SpecimenGroupRepository
  extends ReadWriteRepository[SpecimenGroupId, SpecimenGroup](v => v.id) {

  val log = LoggerFactory.getLogger(this.getClass)

  def specimenGroupWithId(
    studyId: StudyId,
    specimenGroupId: SpecimenGroupId): DomainValidation[SpecimenGroup] = {
    getByKey(specimenGroupId) match {
      case Failure(x) =>
        DomainError("specimen group does not exist: { studyId: %s, specimenGroupId: %s }".format(
          studyId, specimenGroupId)).fail
      case Success(sg) =>
        if (sg.studyId.equals(studyId)) sg.success
        else DomainError(
          "study does not have specimen group: { studyId: %s, specimenGroupId: %s }".format(
            studyId, specimenGroupId)).fail
    }

  }

  def allSpecimenGroupsForStudy(studyId: StudyId): Set[SpecimenGroup] = {
    getValues.filter(x => x.studyId.equals(studyId)).toSet
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
      updatedItem <- SpecimenGroup(specimenGroup.id, specimenGroup.version + 1, specimenGroup.studyId,
        specimenGroup.name, specimenGroup.description, specimenGroup.units,
        specimenGroup.anatomicalSourceType, specimenGroup.preservationType,
        specimenGroup.preservationTemperatureType, specimenGroup.specimenType).success
      repoItem <- updateMap(updatedItem).success
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