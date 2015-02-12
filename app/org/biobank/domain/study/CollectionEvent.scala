package org.biobank.domain.study

import org.biobank.domain.{
  ConcurrencySafeEntity,
  DomainValidation,
  ValidationKey
}
import org.biobank.infrastructure.JsonUtils._

import org.joda.time.DateTime
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import scalaz._
import scalaz.Scalaz._

trait CollectionEventValidations {

  case object VisitNumberInvalid extends ValidationKey

}

/**
 * A collection event is used to record a visit by a participant to a Centre (e.g. a clinic). A collection
 * event must have a CollectionEventType as defined by the study.
 *
 * @param timeDone a time stamp for when the participant made the visit to the centre.
 */
case class CollectionEvent(participantId: ParticipantId,
                           id:            CollectionEventId,
                           version:       Long,
                           timeAdded:     DateTime,
                           timeModified:  Option[DateTime],
                           timeDone:      DateTime,
                           visitNumber:   Int)
    extends ConcurrencySafeEntity[CollectionEventId]
    with HasParticipantId {

  def update(timeDone:      DateTime,
             visitNumber:   Int)
      : DomainValidation[CollectionEvent] = {
    val v = CollectionEvent.create(this.participantId,
                                   this.id,
                                   this.version,
                                   this.timeAdded,
                                   timeDone,
                                   visitNumber)
    v.map(_.copy(timeModified = Some(DateTime.now)))
  }
  override def toString: String =
    s"""|CollectionEvent:{
        |  participantId: $participantId,
        |  id:            $id,
        |  version:       $version,
        |  timeAdded:     $timeAdded,
        |  timeModified:  $timeModified,
        |  timeDone:      $timeDone,
        |  visitNumber:   $visitNumber,
        |}""".stripMargin

}

object CollectionEvent extends CollectionEventValidations {
  import org.biobank.domain.CommonValidations._

  def create(participantId: ParticipantId,
             id:            CollectionEventId,
             version:       Long,
             dateTime:      DateTime,
             timeDone:      DateTime,
             visitNumber:   Int)
      : DomainValidation[CollectionEvent] = {
    (validateId(participantId) |@|
      validateId(id) |@|
      validateAndIncrementVersion(version) |@|
      validatePositiveNumber(visitNumber, VisitNumberInvalid)) {
      CollectionEvent(_, _, _, dateTime, None, timeDone, _)
    }
  }

  implicit val collectionEventWrites = Json.writes[CollectionEvent]
}
