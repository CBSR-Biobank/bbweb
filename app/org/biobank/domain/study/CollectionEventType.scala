package org.biobank.domain.study

import org.biobank.infrastructure.{
  CollectionEventTypeSpecimenGroupData,
  CollectionEventTypeAnnotationTypeData}
import org.biobank.domain.{
  AnnotationTypeId,
  ConcurrencySafeEntity,
  DomainValidation,
  HasName,
  HasDescriptionOption,
  ValidationKey
}
import org.biobank.infrastructure.JsonUtils._

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import org.joda.time.DateTime
import scalaz._
import scalaz.Scalaz._

trait CollectionEventTypeValidations {

  case object MaxCountInvalid extends ValidationKey

  case object AmountInvalid extends ValidationKey
}


/**
  * Defines a classification name, unique to the Study, to a participant visit.
  *
  * A participant visit is a record of when specimens were collected from a
  * [[org.biobank.domain.participant.Participant]] at a collection [[org.biobank.domain.centre.Centre]].  Each
  * collection event type is assigned one or more [[SpecimenGroup]]s to specify the [[SpecimenType]]s that are
  * collected.
  *
  * A study must have at least one collection event type defined in order to record collected specimens.
  *
  * @param recurring Set to true when the collection event type occurs more than once during the
  *        lifetime of the study. False otherwise.

  * @param specimenGroupData One or more [[SpecimenGroup]]s that need to be collected with this
  *        type of collection event. See [[org.biobank.infrastructure.CollectionEventTypeSpecimenGroupData]].

  * @param annotationTypeData The [[AnnotationType]]s for a collection event type.
  *
  */
case class CollectionEventType(studyId:            StudyId,
                               id:                 CollectionEventTypeId,
                               version:            Long,
                               timeAdded:          DateTime,
                               timeModified:       Option[DateTime],
                               name:               String,
                               description:        Option[String],
                               recurring:          Boolean,
                               specimenGroupData:  List[CollectionEventTypeSpecimenGroupData],
                               annotationTypeData: List[CollectionEventTypeAnnotationTypeData])
    extends ConcurrencySafeEntity[CollectionEventTypeId]
    with HasName
    with HasDescriptionOption
    with HasStudyId {

  def update(name:               String,
             description:        Option[String],
             recurring:          Boolean,
             specimenGroupData:  List[CollectionEventTypeSpecimenGroupData],
             annotationTypeData: List[CollectionEventTypeAnnotationTypeData])
      : DomainValidation[CollectionEventType] = {
    val v = CollectionEventType.create(this.studyId,
                                         this.id,
                                         this.version,
                                         this.timeAdded,
                                         name,
                                         description,
                                         recurring,
                                         specimenGroupData,
                               annotationTypeData)
    v.map(_.copy(timeModified = Some(DateTime.now)))
  }

  override def toString: String =
    s"""|CollectionEventType:{
        |  studyId: $studyId,
        |  id: $id,
        |  version: $version,
        |  timeAdded: $timeAdded,
        |  timeModified: $timeModified,
        |  name: $name,
        |  description: $description,
        |  recurring: $recurring,
        |  specimenGroupData: { $specimenGroupData },
        |  annotationTypeData: { $annotationTypeData }
        |}""".stripMargin

}

object CollectionEventType extends CollectionEventTypeValidations with StudyAnnotationTypeValidations {
  import org.biobank.domain.CommonValidations._

  def create(
    studyId: StudyId,
    id: CollectionEventTypeId,
    version: Long,
    dateTime: DateTime,
    name: String,
    description: Option[String],
    recurring: Boolean,
    specimenGroupData: List[CollectionEventTypeSpecimenGroupData],
    annotationTypeData: List[CollectionEventTypeAnnotationTypeData]): DomainValidation[CollectionEventType] = {
    (validateId(studyId) |@|
      validateId(id) |@|
      validateAndIncrementVersion(version) |@|
      validateString(name, NameRequired) |@|
      validateNonEmptyOption(description, NonEmptyDescription) |@|
      validateSpecimenGroupData(specimenGroupData) |@|
      validateAnnotationTypeData(annotationTypeData)) {
      CollectionEventType(_, _, _, dateTime, None, _, _, recurring, _, _)
    }
  }

  /**
    *  Validates each item in the set and returns all failures.
    */
  protected def validateSpecimenGroupData(
    specimenGroupData: List[CollectionEventTypeSpecimenGroupData]): ValidationNel[String, List[CollectionEventTypeSpecimenGroupData]] = {

    def validateSpecimenGroupItem(
      specimenGroupItem: CollectionEventTypeSpecimenGroupData): DomainValidation[CollectionEventTypeSpecimenGroupData] = {
      (validateString(specimenGroupItem.specimenGroupId, IdRequired) |@|
        validatePositiveNumber(specimenGroupItem.maxCount, MaxCountInvalid) |@|
        validatePositiveNumberOption(specimenGroupItem.amount, AmountInvalid)) {
        CollectionEventTypeSpecimenGroupData(_, _, _)
      }
    }

    specimenGroupData.map(validateSpecimenGroupItem).sequenceU
  }

  implicit val collectionEventTypeWrites = Json.writes[CollectionEventType]

}

