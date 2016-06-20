package org.biobank.domain.participants

import org.biobank.domain.{
  Annotation,
  ConcurrencySafeEntity,
  DomainValidation,
  HasAnnotations
}
import org.biobank.domain.study._
import org.biobank.infrastructure.JsonUtils._
import org.joda.time.DateTime
import play.api.libs.json._
import scalaz.Scalaz._

/** The subject for which a set of specimens were collected from. The subject can be human or non human.
 * A participant belongs to a single study.
 *
 * @param uniqueId A participant has a unique identifier that is used to identify the participant in
 *        the system. This identifier is not the same as the ParticipantId value object
 *        used by the domain model.
 */
case class Participant(id:           ParticipantId,
                       studyId:      StudyId,
                       version:      Long,
                       timeAdded:    DateTime,
                       timeModified: Option[DateTime],
                       uniqueId:     String,
                       annotations:  Set[Annotation])
    extends ConcurrencySafeEntity[ParticipantId]
    with HasStudyId
    with ParticipantValidations
    with HasAnnotations[Participant] {
  import org.biobank.domain.CommonValidations._

  def withUniqueId(uniqueId: String): DomainValidation[Participant] = {
    validateString(uniqueId, UniqueIdRequired).map { _ =>
      copy(uniqueId     = uniqueId,
           version      = version + 1,
           timeModified = Some(DateTime.now))
    }
  }

  def withAnnotation(annotation: Annotation): DomainValidation[Participant] = {
    Annotation.validate(annotation).map { _ =>
      val newAnnotations = annotations - annotation + annotation
      copy(annotations  = newAnnotations,
           version      = version + 1,
           timeModified = Some(DateTime.now))
    }
  }

  def withoutAnnotation(annotationTypeId: String): DomainValidation[Participant] = {
    checkRemoveAnnotation(annotationTypeId).map { annotation =>
      val newAnnotations = annotations - annotation
      copy(annotations  = newAnnotations,
           version      = version + 1,
           timeModified = Some(DateTime.now))
    }
  }

  override def toString: String =
    s"""|Participant:{
        |  studyId:      $studyId,
        |  id:           $id,
        |  version:      $version,
        |  timeAdded:    $timeAdded,
        |  timeModified: $timeModified,
        |  uniqueId:     $uniqueId,
        |  annotations:  $annotations,
        |}""".stripMargin
}

object Participant extends ParticipantValidations {
  import org.biobank.domain.CommonValidations._
  import Annotation._

  def create(studyId:     StudyId,
             id:          ParticipantId,
             version:     Long,
             uniqueId:    String,
             annotations: Set[Annotation])
      : DomainValidation[Participant] = {
    (validateId(id) |@|
       validateId(studyId) |@|
       validateVersion(version) |@|
       validateString(uniqueId, UniqueIdRequired) |@|
       annotations.toList.traverseU(Annotation.validate)) {
      case (_, _, _, _, _) =>
        Participant(id, studyId, version, DateTime.now, None, uniqueId, annotations)
    }
  }

  implicit val participantWrites = Json.writes[Participant]

}
