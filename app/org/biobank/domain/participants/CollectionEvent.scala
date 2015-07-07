package org.biobank.domain.participants

import org.biobank.domain.{
  ConcurrencySafeEntity,
  DomainValidation,
  ValidationKey
}
import org.biobank.domain.study._
import org.biobank.infrastructure.JsonUtils._

import org.joda.time.DateTime
import play.api.libs.json._
import scalaz.Scalaz._

trait CollectionEventValidations {

  case object VisitNumberInvalid extends ValidationKey

}

/**
 * A collection event is used to record a visit by a participant to a {@link Centre} (e.g. a clinic). A
 * collection event must have a CollectionEventType as defined by the study.
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
                           annotations:           Set[CollectionEventAnnotation])
    extends ConcurrencySafeEntity[CollectionEventId]
    with HasParticipantId {

  def update(timeCompleted: DateTime,
             visitNumber:   Int,
             annotations:  Set[CollectionEventAnnotation]): DomainValidation[CollectionEvent] = {
    val v = CollectionEvent.create(this.id,
                                   this.participantId,
                                   this.collectionEventTypeId,
                                   this.version,
                                   this.timeAdded,
                                   timeCompleted,
                                   visitNumber,
                                   annotations)
    v.map(_.copy(timeModified = Some(DateTime.now)))
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

object CollectionEvent extends CollectionEventValidations {
  import org.biobank.domain.CommonValidations._

  case object ParticipantIdRequired extends ValidationKey

  case object CollectinEventTypeIdRequired extends ValidationKey

  def create(id:                    CollectionEventId,
             participantId:         ParticipantId,
             collectionEventTypeId: CollectionEventTypeId,
             version:               Long,
             dateTime:              DateTime,
             timeCompleted:         DateTime,
             visitNumber:           Int,
             annotations:           Set[CollectionEventAnnotation])
      : DomainValidation[CollectionEvent] = {
    (validateId(id) |@|
      validateId(participantId, ParticipantIdRequired) |@|
      validateId(collectionEventTypeId, CollectinEventTypeIdRequired) |@|
      validateAndIncrementVersion(version) |@|
      validateMinimum(visitNumber, 1, VisitNumberInvalid)) {
      CollectionEvent(_, _, _, _, dateTime, None, timeCompleted, _, annotations)
    }
  }

  implicit val collectionEventWrites = Json.writes[CollectionEvent]
}
