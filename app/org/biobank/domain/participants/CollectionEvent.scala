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
    with ParticipantValidations {
  import org.biobank.domain.CommonValidations._

  def withVisitNumber(visitNumber: Int): DomainValidation[CollectionEvent] = {
    validateMinimum(visitNumber, 1, VisitNumberInvalid) fold (
      err    => err.failure,
      cevent => copy(version = version + 1, visitNumber = visitNumber).success
    )
  }

  def withTimeCompleted(timeCompleted: DateTime): DomainValidation[CollectionEvent] =
    copy(version = version + 1, timeCompleted = timeCompleted).success

  def withAnnotations(annotations: Set[Annotation])
      : DomainValidation[CollectionEvent] =
    copy(version = version + 1, annotations = annotations).success

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

object CollectionEvent extends ParticipantValidations {
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
       validateAndIncrementVersion(version) |@|
       validateMinimum(visitNumber, 1, VisitNumberInvalid)) {
      case (_, _, _, _, _) => CollectionEvent(id,
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
