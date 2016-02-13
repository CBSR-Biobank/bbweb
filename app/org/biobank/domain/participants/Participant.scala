package org.biobank.domain.participants

import org.biobank.domain.{
  Annotation,
  ConcurrencySafeEntity,
  DomainValidation
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
    with HasStudyId {

  def update(uniqueId: String, annotations: Set[Annotation])
      : DomainValidation[Participant] = {
    val v = Participant.create(this.studyId,
                               this.id,
                               this.version,
                               uniqueId,
                               annotations)
    v.map(_.copy(timeModified = Some(DateTime.now)))
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

    def validateAnnotation(annotation: Annotation): DomainValidation[Annotation] = {
      Annotation.create(annotation.annotationTypeUniqueId,
                        annotation.stringValue,
                        annotation.numberValue,
                        annotation.selectedValues)
    }

    (validateId(id) |@|
       validateId(studyId) |@|
       validateAndIncrementVersion(version) |@|
       validateString(uniqueId, UniqueIdRequired) |@|
       annotations.toList.traverseU(Annotation.validate)) {
      case (_, _, _, _, _) =>
        Participant(id, studyId, version, DateTime.now, None, uniqueId, annotations)
    }
  }

  implicit val participantWrites = Json.writes[Participant]

}
