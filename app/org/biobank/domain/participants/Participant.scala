package org.biobank.domain.participants

import java.time.OffsetDateTime
import org.biobank.domain._
import org.biobank.domain.study._
import play.api.libs.json._
import scalaz.Scalaz._

/**
 * The subject for which a set of specimens were collected from. The subject can be human or non human. A
 * participant belongs to a single study.
 *
 * @param uniqueId A participant has a unique identifier that is used to identify the participant in the
 *                 system. This identifier is not the same as the ParticipantId value object used by the
 *                 domain model.
 */
final case class Participant(id:           ParticipantId,
                             studyId:      StudyId,
                             version:      Long,
                             timeAdded:    OffsetDateTime,
                             timeModified: Option[OffsetDateTime],
                             slug:         String,
                             uniqueId:     String,
                             annotations:  Set[Annotation])
    extends ConcurrencySafeEntity[ParticipantId]
    with HasSlug
    with HasStudyId
    with ParticipantValidations
    with HasAnnotations[Participant] {
  import org.biobank.CommonValidations._

  def withUniqueId(uniqueId: String): DomainValidation[Participant] = {
    validateNonEmptyString(uniqueId, UniqueIdRequired).map { _ =>
      copy(uniqueId     = uniqueId,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now))
    }
  }

  def withAnnotation(annotation: Annotation): DomainValidation[Participant] = {
    Annotation.validate(annotation).map { _ =>
      val newAnnotations = annotations - annotation + annotation
      copy(annotations  = newAnnotations,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now))
    }
  }

  def withoutAnnotation(annotationTypeId: AnnotationTypeId): DomainValidation[Participant] = {
    checkRemoveAnnotation(annotationTypeId).map { annotation =>
      val newAnnotations = annotations - annotation
      copy(annotations  = newAnnotations,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now))
    }
  }

  override def toString: String =
    s"""|Participant:{
        |  studyId:      $studyId,
        |  id:           $id,
        |  version:      $version,
        |  timeAdded:    $timeAdded,
        |  timeModified: $timeModified,
        |  slug:         $slug,
        |  uniqueId:     $uniqueId,
        |  annotations:  $annotations,
        |}""".stripMargin
}

object Participant extends ParticipantValidations {
  import org.biobank.CommonValidations._
  import org.biobank.domain.DomainValidations._
  import Annotation._

  def create(studyId:     StudyId,
             id:          ParticipantId,
             version:     Long,
             uniqueId:    String,
             annotations: Set[Annotation],
             timeAdded:   OffsetDateTime)
      : DomainValidation[Participant] = {
    (validateId(id) |@|
       validateId(studyId) |@|
       validateVersion(version) |@|
       validateNonEmptyString(uniqueId, UniqueIdRequired) |@|
       annotations.toList.traverseU(Annotation.validate)) {
      case _ => Participant(id,
                            studyId,
                            version,
                            timeAdded,
                            None,
                            Slug(uniqueId),
                            uniqueId,
                            annotations)
    }
  }

  implicit val participantWrites: Format[Participant] = Json.format[Participant]

}
