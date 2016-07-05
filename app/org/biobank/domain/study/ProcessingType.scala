package org.biobank.domain.study

import org.biobank.domain.{
  ConcurrencySafeEntity,
  DomainValidation,
  HasUniqueName,
  HasDescriptionOption
}
import org.biobank.infrastructure.JsonUtils._
import org.joda.time.DateTime

import play.api.libs.json._
import scalaz.Scalaz._

/** Records a regularly preformed specimen processing procedure. There are one or more associated
  * [[SpecimenLinkType]]s that further define legal procedures, and allow recording of procedures
  * performed on different types of [[Specimen]]s.
  *
  * For speicmen processing to take place, a study must have at least one processing type defined.
  *
  *  @param enabled A processing type should have enabled set to true when processing of the
  *         contained specimen types is taking place. However, throughout the lifetime of the study, it may be
  *         decided to stop a processing type in favour of another.  In this case enabled is set to false.
  *
  */
final case class ProcessingType(studyId:      StudyId,
                                id:           ProcessingTypeId,
                                version:      Long,
                                timeAdded:    DateTime,
                                timeModified: Option[DateTime],
                                name:         String,
                                description:  Option[String],
                                enabled:      Boolean)
    extends ConcurrencySafeEntity[ProcessingTypeId]
    with HasUniqueName
    with HasDescriptionOption
    with HasStudyId {

  /** Updates a processing type with new values.
    */
  def update(name: String, description: Option[String], enabled: Boolean)
      : DomainValidation[ProcessingType] = {
    val v = ProcessingType.create(this.studyId,
                                  this.id,
                                  this.version,
                                  name,
                                  description,
                                  enabled)
    v.map(_.copy(timeModified = Some(DateTime.now)))
  }

  override def toString: String =
    s"""|ProcessingType:{
        |  studyId:      $studyId,
        |  id:           $id,
        |  timeAdded:    $timeAdded,
        |  timeModified: $timeModified,
        |  version:      $version,
        |  name:         $name,
        |  description:  $description,
        |  enabled:      $enabled
        |}""".stripMargin
}

object ProcessingType {
  import org.biobank.domain.CommonValidations._

  def create(studyId:     StudyId,
             id:          ProcessingTypeId,
             version:     Long,
             name:        String,
             description: Option[String],
             enabled:     Boolean)
      : DomainValidation[ProcessingType] = {
    (validateId(studyId) |@|
       validateId(id) |@|
       validateVersion(version) |@|
       validateString(name, NameRequired) |@|
       validateNonEmptyOption(description, InvalidDescription) ) {
      case (_, _, _, _, _) => ProcessingType(studyId,
                                             id,
                                             version,
                                             DateTime.now,
                                             None,
                                             name,
                                             description,
                                             enabled)
    }
  }

  implicit val processingTypeWrites: Writes[ProcessingType] = Json.writes[ProcessingType]
}
