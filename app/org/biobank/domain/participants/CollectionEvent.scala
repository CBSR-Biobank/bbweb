package org.biobank.domain.participants

import java.time.OffsetDateTime
import org.biobank.ValidationKey
import org.biobank.domain._
import org.biobank.domain.study._
import play.api.libs.json._
import scalaz.Scalaz._

/**
 * Predicates that can be used to filter collections of studies.
 *
 */
trait CollectionEventPredicates {

  type CollectionEventFilter = CollectionEvent => Boolean

  val visitNumberIsOneOf: Set[String] => CollectionEventFilter =
    visitNumbers => cevent => visitNumbers.contains(cevent.visitNumber.toString)

}

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
final case class CollectionEvent(id:                    CollectionEventId,
                                 participantId:         ParticipantId,
                                 collectionEventTypeId: CollectionEventTypeId,
                                 version:               Long,
                                 timeAdded:             OffsetDateTime,
                                 timeModified:          Option[OffsetDateTime],
                                 slug:                  String,
                                 timeCompleted:         OffsetDateTime,
                                 visitNumber:           Int,
                                 annotations:           Set[Annotation])
    extends ConcurrencySafeEntity[CollectionEventId]
    with HasParticipantId
    with CollectionEventValidations
    with HasSlug
    with HasAnnotations[CollectionEvent] {
  import org.biobank.CommonValidations._

  def withVisitNumber(visitNumber: Int): DomainValidation[CollectionEvent] = {
    validateMinimum(visitNumber, 1, VisitNumberInvalid).map { _ =>
      copy(visitNumber  = visitNumber,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now))
    }
  }

  def withTimeCompleted(timeCompleted: OffsetDateTime): DomainValidation[CollectionEvent] = {
    copy(timeCompleted = timeCompleted,
         version         = version + 1,
         timeModified    = Some(OffsetDateTime.now)).successNel[String]
  }

  def withAnnotation(annotation: Annotation): DomainValidation[CollectionEvent] = {
    Annotation.validate(annotation).map { _ =>
      val newAnnotations = annotations - annotation + annotation
      copy(annotations  = newAnnotations,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now))
    }
  }

  def withoutAnnotation(annotationTypeId: AnnotationTypeId): DomainValidation[CollectionEvent] = {
    checkRemoveAnnotation(annotationTypeId).map { annotation =>
      val newAnnotations = annotations - annotation
      copy(annotations  = newAnnotations,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now))
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
  import org.biobank.CommonValidations._
  import org.biobank.domain.DomainValidations._

  def create(id:                    CollectionEventId,
             participantId:         ParticipantId,
             collectionEventTypeId: CollectionEventTypeId,
             version:               Long,
             timeAdded:             OffsetDateTime,
             timeCompleted:         OffsetDateTime,
             visitNumber:           Int,
             annotations:           Set[Annotation])
      : DomainValidation[CollectionEvent] = {
    (validateId(id) |@|
       validateId(participantId, ParticipantIdRequired) |@|
       validateId(collectionEventTypeId, CollectionEventTypeIdRequired) |@|
       validateVersion(version) |@|
       validateMinimum(visitNumber, 1, VisitNumberInvalid) |@|
       annotations.toList.traverseU(Annotation.validate)) { case _ =>
        CollectionEvent(id,
                        participantId,
                        collectionEventTypeId,
                        version,
                        timeAdded,
                        None,
                        slug = Slug(s"visit-number-${visitNumber}"),
                        timeCompleted,
                        visitNumber,
                        annotations)
    }
  }

  val sort2Compare: Map[String, (CollectionEvent, CollectionEvent) => Boolean] =
    Map[String, (CollectionEvent, CollectionEvent) => Boolean](
      "visitNumber"   -> compareByVisitNumber,
      "timeCompleted" -> compareByTimeCompleted)

  def compareByVisitNumber(a: CollectionEvent, b: CollectionEvent): Boolean =
    (a.visitNumber compareTo b.visitNumber) < 0

  def compareByTimeCompleted(a: CollectionEvent, b: CollectionEvent): Boolean =
    (a.timeCompleted compareTo b.timeCompleted) < 0

  implicit val collectionEventFormat: Format[CollectionEvent] = Json.format[CollectionEvent]
}
