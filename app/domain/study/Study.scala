package domain.study

import scalaz._
import scalaz.Scalaz._
import domain.AmatomicalSourceId
import domain.Entity
import domain.Identity
import domain.PreservationId
import domain.DomainValidation
import domain.DomainError

sealed abstract class Study extends Entity {
  def name: String
  def description: String
  def specimenGroups: List[SpecimenGroup]
}

object Study {
  val invalidVersionMessage = "study %s: expected version %s doesn't match current version %s"

  def invalidVersion(studyId: Identity, expected: Long, current: Long) =
    DomainError(invalidVersionMessage format (studyId, expected, current))

  def requireVersion[T <: Study](study: T, expectedVersion: Option[Long]): DomainValidation[T] = {
    val id = study.id
    val version = study.version

    expectedVersion match {
      case Some(expected) if (version != expected) => invalidVersion(id, expected, version).fail
      case Some(expected) if (version == expected) => study.success
      case None => study.success
    }
  }

  // TODO: not sure yet if this is the right place for this method
  def nextIdentity: StudyId =
    new StudyId(java.util.UUID.randomUUID.toString.toUpperCase)

  def add(id: StudyId, name: String, description: String): DomainValidation[DisabledStudy] =
    DisabledStudy(id, version = 0L, name, description, specimenGroups = Nil).success

}

case class DisabledStudy(id: StudyId, version: Long = -1, name: String, description: String,
  specimenGroups: List[SpecimenGroup] = Nil)
  extends Study {

  def addSpecimenGroup(name: String, description: String, unit: String,
    amatomicalSourceId: AmatomicalSourceId, preservationId: PreservationId,
    specimenTypeId: SpecimenTypeId): DomainValidation[DisabledStudy] = {
    val specimenGroup = new SpecimenGroup(SpecimenGroup.nextIdentity, name, description, unit,
      amatomicalSourceId, preservationId, specimenTypeId)
    copy(version = version + 1, specimenGroups = specimenGroups :+ specimenGroup).success
  }

  def addCollectionEventType(name: String, description: String, recurring: Boolean) = {

  }

  override def toString =
    "{ id:%s, name:%s, description:%s }" format (id.toString, name, description)

}

case class EnabledStudy(id: StudyId, version: Long = -1, name: String, description: String,
  specimenGroups: List[SpecimenGroup] = Nil)
  extends Study {

  override def toString =
    "{ id:%s, name:%s, description:%s }" format (id.toString, name, description)

}

// study commands
case class AddStudy(name: String, description: String)
case class UpdateStudy(id: StudyId, expectedVersion: Option[Long], name: String, description: String)
case class EnableStudy(id: StudyId, expectedVersion: Option[Long])
case class DisableStudy(id: StudyId, expectedVersion: Option[Long])

// specimen group commands
case class AddSpecimenGroup(studyId: StudyId, expectedVersion: Option[Long],
  name: String, description: String, units: String, amatomicalSourceId: AmatomicalSourceId,
  preservationId: PreservationId, specimenTypeId: SpecimenTypeId)
case class UpdateSpecimenGroup(studyId: StudyId, expectedVersion: Option[Long],
  specimenGroupId: SpecimenGroupId, name: String, description: String, units: String, amatomicalSourceId: AmatomicalSourceId, preservationId: PreservationId,
  specimenTypeId: SpecimenTypeId)
case class RemoveSpecimenGroup(studyId: StudyId, expectedVersion: Option[Long],
  specimenGroupId: SpecimenGroupId)

// collection event commands
case class AddCollectionEventType(studyId: StudyId, expectedVersion: Option[Long], name: String,
  description: String, recurring: Boolean);
case class UpdateCollectionEventType(studyId: StudyId, expectedVersion: Option[Long],
  collectionEventId: CollectionEventId, name: String, description: String, recurring: Boolean);

// study events
case class StudyAdded(name: String, description: String)

