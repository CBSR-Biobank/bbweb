package domain.study

import domain.AnatomicalSourceId
import domain.DomainError
import domain.DomainValidation
import domain.ConcurrencySafeEntity
import domain.Identity
import domain.AnatomicalSourceType._
import domain.PreservationType._
import domain.PreservationTemperatureType._
import domain.SpecimenType._

import infrastructure.commands._

import scalaz._
import scalaz.Scalaz._

sealed abstract class Study extends ConcurrencySafeEntity[StudyId] {
  def name: String
  def description: String

  override def toString =
    "{ id:%s, version: %d, name:%s, description:%s }" format (id, version, name, description)
}

object Study {

  def add(name: String, description: String): DomainValidation[DisabledStudy] =
    DisabledStudy(StudyIdentityService.nextIdentity, version = 0L, name, description).success
}

case class DisabledStudy(id: StudyId, version: Long = -1, name: String, description: String)
  extends Study {

  def enable(specimenGroupCount: Int, collectionEventTypecount: Int): DomainValidation[EnabledStudy] =
    if ((specimenGroupCount == 0) || (collectionEventTypecount == 0))
      DomainError("study has no specimen groups and / or no collection event types").fail
    else
      EnabledStudy(id, version + 1, name, description).success

  def addSpecimenGroup(specimenGroups: Map[SpecimenGroupId, SpecimenGroup],
    cmd: AddSpecimenGroupCmd): DomainValidation[SpecimenGroup] =
    specimenGroups.find(sg => sg._2.name.equals(name)) match {
      case Some(sg) => DomainError("specimen group with name already exists: %s" format name).fail
      case None =>
        SpecimenGroup.add(this.id, cmd.name, cmd.description, cmd.units, cmd.anatomicalSourceType,
          cmd.preservationType, cmd.preservationTemperatureType, cmd.specimenType)
    }

  def addCollectionEventType(
    collectionEventTypes: Map[CollectionEventTypeId, CollectionEventType],
    name: String, description: String,
    recurring: Boolean): DomainValidation[CollectionEventType] =
    collectionEventTypes.find(cet => cet._2.name.equals(name)) match {
      case Some(sg) => DomainError("collection event type with name already exists: %s" format name).fail
      case None =>
        CollectionEventType.add(this.id, name, description, recurring)
    }

}

case class EnabledStudy(id: StudyId, version: Long = -1, name: String, description: String)
  extends Study {

  def disable: DomainValidation[DisabledStudy] =
    DisabledStudy(id, version + 1, name, description).success
}

