package org.biobank.domain.study

import org.biobank.infrastructure.{
  CollectionEventTypeSpecimenGroupData,
  CollectionEventTypeAnnotationTypeData}
import org.biobank.domain._
import org.biobank.infrastructure.JsonUtils._

import play.api.libs.json._
import org.joda.time.DateTime
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

/** A Study represents a collection of participants and specimens collected for a particular research
  * study. This is an aggregate root.
  *
  * A study can be in one of 3 states: diabled, enabled, or retired. These are represented by
  * the sub classes.
  *
  */
sealed trait Study
    extends ConcurrencySafeEntity[StudyId]
    with HasUniqueName
    with HasDescriptionOption {

  override def toString =
    s"""|${this.getClass.getSimpleName}: {
        |  id:           $id,
        |  version:      $version,
        |  timeAdded:    $timeAdded,
        |  timeModified: $timeModified,
        |  name:         $name,
        |  description:  $description
        |}""".stripMargin

}

object Study {

  implicit val studyWrites = new Writes[Study] {
    def writes(study: Study) = Json.obj(
      "id"           -> study.id,
      "version"      -> study.version,
      "timeAdded"    -> study.timeAdded,
      "timeModified" -> study.timeModified,
      "name"         -> study.name,
      "description"  -> study.description,
      "status"       -> study.getClass.getSimpleName
    )
  }

  def compareByName(a: Study, b: Study) = (a.name compareToIgnoreCase b.name) < 0

  def compareByStatus(a: Study, b: Study) = {
    val statusCompare = a.getClass.getSimpleName compare b.getClass.getSimpleName
    if (statusCompare == 0) {
      compareByName(a, b)
    } else {
      statusCompare < 0
    }
  }
}

trait StudyValidations {

  val NameMinLength = 2

  case object InvalidStudyId extends ValidationKey

  case object InvalidSpecimenGroupId extends ValidationKey
}

/**
  * This is the initial state for a study.  In this state, only configuration changes are allowed.
  * Collection and processing of specimens cannot be recorded.
  *
  * This class has a private constructor and instances of this class can only be created using
  * the [[DisabledStudy.create]] method on the factory object.
  */
case class DisabledStudy(id:           StudyId,
                         version:      Long,
                         timeAdded:    DateTime,
                         timeModified: Option[DateTime],
                         name:         String,
                         description:  Option[String])
    extends Study
    with StudyValidations {
  import CommonValidations._

  /** Used to change the name. */
  def withName(name: String): DomainValidation[DisabledStudy] = {
    validateString(name, NameMinLength, InvalidName) fold (
      err => err.failure,
      s   => copy(version = version + 1, name = name).success
    )
  }

  /** Used to change the description. */
  def withDescription(description: Option[String]): DomainValidation[DisabledStudy] = {
    validateNonEmptyOption(description, InvalidDescription) fold (
      err => err.failure,
      s   => copy(version = version + 1, description  = description).success
    )
  }

  /** Used to enable a study after it has been configured, or had configuration changes made on it. */
  def enable(specimenGroupCount:       Int, collectionEventTypeCount: Int)
      : DomainValidation[EnabledStudy] = {
    for {
      sgCount <- {
        if (specimenGroupCount > 0) true.success
        else DomainError("no specimen groups").failureNel
      }
      cetCount <- {
        if (collectionEventTypeCount > 0) true.success
        else DomainError("no collection event types").failureNel
      }
      enabledStudy <- EnabledStudy(id           = this.id,
                                   version      = this.version + 1,
                                   timeAdded    = this.timeAdded,
                                   timeModified = this.timeModified,
                                   name         = this.name,
                                   description  = this.description).success
    } yield enabledStudy
  }

  /** When a study will no longer collect specimens from participants it can be retired. */
  def retire(): DomainValidation[RetiredStudy] = {
    RetiredStudy(id           = this.id,
                 version      = this.version + 1,
                 timeAdded    = this.timeAdded,
                 timeModified = this.timeModified,
                 name         = this.name,
                 description  = this.description).success
  }

}

/**
  * Factory object used to create a study.
  */
object DisabledStudy extends StudyValidations {
  import CommonValidations._

  /**
    * The factory method to create a study.
    *
    * Performs validation on fields.
    */
  def create(id:          StudyId,
             version:     Long,
             name:        String,
             description: Option[String])
      : DomainValidation[DisabledStudy] = {
    (validateId(id) |@|
      validateAndIncrementVersion(version) |@|
      validateString(name, NameMinLength, InvalidName) |@|
      validateNonEmptyOption(description, InvalidDescription)) {
        DisabledStudy(_, _, DateTime.now, None, _, _)
      }
  }
}

/**
  * When a study is in this state, collection and processing of specimens can be recorded.
  *
  * This class has a private constructor and instances of this class can only be created using
  * the [[EnabledStudy.create]] method on the factory object.
  */
case class EnabledStudy(id:           StudyId,
                        version:      Long,
                        timeAdded:    DateTime,
                        timeModified: Option[DateTime],
                        name:         String,
                        description:  Option[String])
    extends Study {

  def disable(): DomainValidation[DisabledStudy] = {
    DisabledStudy(id           = this.id,
                  version      = this.version + 1,
                  timeAdded    = this.timeAdded,
                  timeModified = this.timeModified,
                  name         = this.name,
                  description  = this.description).success
  }
}

/**
 *  In this state the study cannot be modified and collection and processing of specimens is not allowed.
  *
  * This class has a private constructor and instances of this class can only be created using
  * the [[RetiredStudy.create]] method on the factory object.
 */
case class RetiredStudy(id:           StudyId,
                        version:      Long,
                        timeAdded:    DateTime,
                        timeModified: Option[DateTime],
                        name:         String,
                        description:  Option[String])
    extends Study {

  def unretire(): DomainValidation[DisabledStudy] = {
    DisabledStudy(id           = this.id,
                  version      = this.version + 1,
                  timeAdded    = this.timeAdded,
                  timeModified = this.timeModified,
                  name         = this.name,
                  description  = this.description).success
  }
}
