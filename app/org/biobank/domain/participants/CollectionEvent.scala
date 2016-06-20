package org.biobank.domain.participants

import org.biobank.ValidationKey
import org.biobank.domain.{
  Annotation,
  ConcurrencySafeEntity,
  HasAnnotations,
  DomainValidation
}
import org.biobank.domain.study._
import org.biobank.infrastructure.JsonUtils._
import org.joda.time.DateTime
import play.api.libs.json._
import scalaz.Scalaz._

trait CollectionEventValidations extends ParticipantValidations {

  case object VisitNumberInvalid extends ValidationKey

}

/**
 * A collection event is used to record a visit by a [[Participant]] to a [[centre.Centre]] (e.g. a clinic). A
 * collection event must have a [[study.CollectionEventType]] as defined by the [[study.Study]].
 *
 * @param timeCompleted a time stamp for when the participant made the visit to the centre.
 * @param visitNumber an positive integer used to uniquely identify the visit. The fist visit starts at 1.
 */
case class CollectionEvent(id:                    CollectionEventId,
                           participantId:         ParticipantId,
                           collectionEventTypeId: CollectionEventTypeId,
                           version:               Long,
                           timeAdded:             DateTime,
                           timeModified:          Option[DateTime],
                           timeCompleted:         DateTime,
                           visitNumber:           Int,
                           annotations:           Set[Annotation])
    extends ConcurrencySafeEntity[CollectionEventId]
    with HasParticipantId
    with CollectionEventValidations
    with HasAnnotations[CollectionEvent] {
  import org.biobank.domain.CommonValidations._

  def withVisitNumber(visitNumber: Int): DomainValidation[CollectionEvent] = {
    validateMinimum(visitNumber, 1, VisitNumberInvalid).map { _ =>
      copy(visitNumber  = visitNumber,
           version      = version + 1,
           timeModified = Some(DateTime.now))
    }
  }

  def withTimeCompleted(timeCompleted: DateTime): DomainValidation[CollectionEvent] = {
    copy(timeCompleted = timeCompleted,
         version         = version + 1,
         timeModified    = Some(DateTime.now)).success
  }

  def withAnnotation(annotation: Annotation): DomainValidation[CollectionEvent] = {
    Annotation.validate(annotation).map { _ =>
      val newAnnotations = annotations - annotation + annotation
      copy(annotations  = newAnnotations,
           version      = version + 1,
           timeModified = Some(DateTime.now))
    }
  }

  def withoutAnnotation(annotationTypeId: String): DomainValidation[CollectionEvent] = {
    checkRemoveAnnotation(annotationTypeId).map { annotation =>
      val newAnnotations = annotations - annotation
      copy(annotations  = newAnnotations,
           version      = version + 1,
           timeModified = Some(DateTime.now))
    }
  }

  override def toString: String =
    s"""|CollectionEvent:{
        |  id:                    $id,
        |  participantId:         $participantId,
        |  collectionEventTypeId: $collectionEventTypeId
        |  version:               $version,
        |  timeAdded:             $timeAdded,
        |  timeModified:          $timeModified,
        |  timeCompleted:         $timeCompleted,
        |  visitNumber:           $visitNumber,
        |  annotations:           $annotations,
        |}""".stripMargin

}

object CollectionEvent
    extends CollectionEventValidations {
  import org.biobank.domain.CommonValidations._

  def create(id:                    CollectionEventId,
             participantId:         ParticipantId,
             collectionEventTypeId: CollectionEventTypeId,
             version:               Long,
             timeCompleted:         DateTime,
             visitNumber:           Int,
             annotations:           Set[Annotation])
      : DomainValidation[CollectionEvent] = {
    (validateId(id) |@|
       validateId(participantId, ParticipantIdRequired) |@|
       validateId(collectionEventTypeId, CollectionEventTypeIdRequired) |@|
       validateVersion(version) |@|
       validateMinimum(visitNumber, 1, VisitNumberInvalid) |@|
       annotations.toList.traverseU(Annotation.validate)) {
      case (_, _, _, _, _, _) => CollectionEvent(id,
                                                 participantId,
                                                 collectionEventTypeId,
                                                 version,
                                                 DateTime.now,
                                                 None,
                                                 timeCompleted,
                                                 visitNumber,
                                                 annotations)
    }
  }

  def compareByVisitNumber(a: CollectionEvent, b: CollectionEvent) =
    (a.visitNumber compareTo b.visitNumber) < 0

  def compareByTimeCompleted(a: CollectionEvent, b: CollectionEvent) =
    (a.timeCompleted compareTo b.timeCompleted) < 0

  implicit val collectionEventWrites = Json.writes[CollectionEvent]
}
