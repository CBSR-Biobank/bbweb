package org.biobank.domain.centre

import org.biobank.domain.{
  Comment,
  ConcurrencySafeEntity,
  HasUniqueName,
  HasDescriptionOption,
  Location
}
import org.biobank.domain.study.StudyId

/**
  * A Centre can be one or a combination of the following:
  *
  * - Collection Centre: where specimens are collected from patients (e.g. a clinic).
  *
  * - Processing Centre: where collected specimens are processed.
  *
  * - Storage Centre: where collected and processed specimens are stored. Usually, a storage centre is also a
  *   processing centre.
  *
  * - Request Centre: where collected and processed specimens are sent for analysis. Usually the specimens are
  *   stored first at a storage centre for a period of time.
  *
  */
sealed trait Centre
    extends ConcurrencySafeEntity[CentreId]
    with HasUniqueName
    with HasDescriptionOption {

  /*** @param studyIds Links a centre and a study. When linked, the center can then participate in the study. */
  val studyIds: Set[StudyId]

  /** @param locations Used to record a location. A centre may have more than one location. */
  val locations: List[Location]

  /** @params comments A set of comments made by users. */
  val comments: List[Comment]

  /** @param status Contains the current state of the object, one of: Disabled, Enabled. */
  val status: String

  override def toString =
    s"""|${this.getClass.getSimpleName}: {
        |  id: $id,
        |  version: $version,
        |  addedDate: $addedDate,
        |  lastUpdateDate: $lastUpdateDate,
        |  name: $name,
        |  description: $description
        |}""".stripMargin

}


/**
  * This is the initial state for a centre.  In this state, only configuration changes are allowed.
  * Collection and processing of specimens cannot be recorded.
  *
  * This class has a private constructor and instances of this class can only be created using
  * the [[DisabledCentre.create]] method on the factory object.
  */
case class DisabledCentre private (
  id: CentreId,
  version: Long,
  addedDate: DateTime,
  lastUpdateDate: Option[DateTime],
  name: String,
  description: Option[String],
  studyIds: Set[StudyId],
  locations: List[Location],
  comments: List[Comment])
    extends Centre {

  override val status: String = "Disabled"

  /** Used to change the name or the description. */
  def update(
    expectedVersion: Option[Long],
    dateTime: DateTime,
    name: String,
    description: Option[String]): DomainValidation[DisabledCentre] = {
    for {
      validVersion <- requireVersion(expectedVersion)
      validatedCentre <- DisabledCentre.create(id, version, addedDate, name, description)
      updatedCentre <- validatedCentre.copy(lastUpdateDate = Some(dateTime)).success
    } yield updatedCentre
  }

  def addStudy(studyId: StudyId): Centre = {
    this.copy(studyIds = this.studyIds + studyId)
  }

  def removeStudy(studyId: StudyId): Centre = {
    this.copy(studyIds = this.studyIds + studyId)
  }

  def addLocation(location: Location): Centre = {
    this.copy(locations = this.locations + location)
  }

  def removeLocation(location: Location): Centre = {
    this.copy(locations = this.locations - location)
  }

  def addComment(comment: Comment): Centre = {
    this.copy(comments = this.comments + comment)
  }

  /** Used to enable a centre after it has been configured, or had configuration changes made on it. */
  def enable(
    expectedVersion: Option[Long],
    dateTime: DateTime): DomainValidation[EnabledCentre] = {

    def checkSpecimenGroupCount =
      if (specimenGroupCount > 0) true.success else DomainError("no specimen groups").failNel

    def checkCollectionEventTypeCount =
      if (collectionEventTypeCount > 0) true.success else DomainError("no collection event types").failNel

    for {
      validVersion <- requireVersion(expectedVersion)
      enabledCentre <- EnabledCentre.create(this, dateTime)
    } yield enabledCentre
  }
}

/**
  * When a centre is in this state, collection and processing of specimens can be recorded.
  *
  * This class has a private constructor and instances of this class can only be created using
  * the [[EnabledCentre.create]] method on the factory object.
  */
case class EnabledCentre private (
  id: CentreId,
  version: Long,
  addedDate: DateTime,
  lastUpdateDate: Option[DateTime],
  name: String,
  description: Option[String])
  extends Centre {

  override val status: String = "Enabled"

  def disable(
    expectedVersion: Option[Long],
    dateTime: DateTime): DomainValidation[DisabledCentre] = {
    for {
      validVersion <- requireVersion(expectedVersion)
      validatedCentre <- DisabledCentre.create(id, version, addedDate, name, description)
      disabledCentre <- validatedCentre.copy(lastUpdateDate = Some(dateTime)).success
    } yield disabledCentre
  }
}
