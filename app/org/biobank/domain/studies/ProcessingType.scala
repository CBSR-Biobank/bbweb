package org.biobank.domain.studies

import java.time.OffsetDateTime
import org.biobank.domain._
import play.api.libs.json._
import scalaz.Scalaz._

/** Records a regularly preformed specimen processing procedure. There are one or more associated
  * [[SpecimenLinkType]]s that further define legal procedures, and allow recording of procedures
  * performed on different types of [[domain.participants.Specimen Specimen]]s.
 *
 * For speicmen processing to take place, a study must have at least one processing type defined.
   *
  *  @param enabled A processing type should have enabled set to true when processing of the
  *         contained specimen types is taking place. However, throughout the lifetime of the study, it may be
  *         decided to stop a processing type in favour of another.  In this case enabled is set to false.
 *
 */
final case class ProcessingType(studyId:      StudyId,
                                          id:                         ProcessingTypeId,
                                          version:                    Long,
                                          timeAdded:                  OffsetDateTime,
                                          timeModified:               Option[OffsetDateTime],
                                          slug:                       String,
                                          name:                       String,
                                          description:                Option[String],
                                enabled:      Boolean)
    extends ConcurrencySafeEntity[ProcessingTypeId]
    with HasUniqueName
    with HasOptionalDescription
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
    v.map(_.copy(timeModified = Some(OffsetDateTime.now)))
  }

  override def toString: String =
    s"""|ProcessingType:{
        |  studyId:                     $studyId,
        |  id:                          $id,
        |  timeAdded:                   $timeAdded,
        |  timeModified:                $timeModified,
        |  version:                     $version,
        |  name:                        $name,
        |  description:                 $description,
        |  enabled:      $enabled
        |}""".stripMargin
}

object ProcessingType {
  import org.biobank.CommonValidations._
  import org.biobank.domain.DomainValidations._

  def create(studyId:                    StudyId,
             id:                         ProcessingTypeId,
             version:                    Long,
             name:                       String,
             description:                Option[String],
             enabled:     Boolean)
      : DomainValidation[ProcessingType] = {
    (validateId(studyId) |@|
       validateId(id) |@|
       validateVersion(version) |@|
       validateString(name, NameRequired) |@|
       validateNonEmptyStringOption(description, InvalidDescription)) { case _ =>
        ProcessingType(studyId      = studyId,
                                 id                         = id,
                                 version                    = version,
                                 timeAdded                  = OffsetDateTime.now,
                                 timeModified               = None,
                                 slug                       = Slug(name),
                                 name                       = name,
                                 description                = description,
                       enabled      = enabled)
  }
}

  implicit val processingTypeWrites: Writes[ProcessingType] = Json.writes[ProcessingType]
}
