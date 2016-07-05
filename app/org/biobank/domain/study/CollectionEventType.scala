package org.biobank.domain.study

import org.biobank.ValidationKey
import org.biobank.domain.{
  AnnotationType,
  ConcurrencySafeEntity,
  DomainError,
  DomainValidation,
  HasName,
  HasDescriptionOption,
  HasAnnotationTypes
}
import org.biobank.infrastructure.JsonUtils._

import play.api.libs.json._
import org.joda.time.DateTime
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

trait CollectionEventTypeValidations {

  case object MaxCountInvalid extends ValidationKey

  case object AmountInvalid extends ValidationKey

  case object StudyIdRequired extends ValidationKey
}


/**
  * Defines a classification name, unique to the Study, to a participant visit.
  *
  * A participant visit is a record of when specimens were collected from a
  * [[org.biobank.domain.participant.Participant]] at a collection [[org.biobank.domain.centre.Centre]]. Each
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
final case class CollectionEventType(studyId:            StudyId,
                                     id:                 CollectionEventTypeId,
                                     version:            Long,
                                     timeAdded:          DateTime,
                                     timeModified:       Option[DateTime],
                                     name:               String,
                                     description:        Option[String],
                                     recurring:          Boolean,
                                     specimenSpecs:      Set[CollectionSpecimenSpec],
                                     annotationTypes:    Set[AnnotationType])
    extends ConcurrencySafeEntity[CollectionEventTypeId]
    with HasName
    with HasDescriptionOption
    with HasStudyId
    with HasAnnotationTypes {
  import org.biobank.domain.CommonValidations._

  def withName(name: String): DomainValidation[CollectionEventType] = {
    validateString(name, NameRequired).map { _ =>
      copy(name         = name,
           version      = version + 1,
           timeModified = Some(DateTime.now))
    }
  }

  def withDescription(description: Option[String]): DomainValidation[CollectionEventType] = {
    validateNonEmptyOption(description, InvalidDescription).map { _ =>
      copy(description  = description,
           version      = version + 1,
           timeModified = Some(DateTime.now))
    }
  }

  def withRecurring(recurring: Boolean): DomainValidation[CollectionEventType] = {
    copy(recurring   = recurring,
         version      = version + 1,
         timeModified = Some(DateTime.now)).successNel[String]
  }

  def withAnnotationType(annotationType: AnnotationType)
      : DomainValidation[CollectionEventType] = {
    checkAddAnnotationType(annotationType).map { _ =>
      // replaces previous annotation type with same unique id
      val newAnnotationTypes = annotationTypes - annotationType + annotationType
      copy(annotationTypes = newAnnotationTypes,
           version         = version + 1,
           timeModified    = Some(DateTime.now))
    }
  }

  def removeAnnotationType(annotationTypeId: String)
      : DomainValidation[CollectionEventType] = {
    checkRemoveAnnotationType(annotationTypeId).map { annotationType =>
      val newAnnotationTypes = annotationTypes - annotationType
      copy(annotationTypes = newAnnotationTypes,
           version         = version + 1,
           timeModified    = Some(DateTime.now))
    }
  }

  // replaces a previous one with the same unique id if it exists
  //
  // fails if there is another with the same name
  def withSpecimenSpec(specimenSpec: CollectionSpecimenSpec)
      : DomainValidation[CollectionEventType] = {
    for {
      nameNotUsed <- {
        specimenSpecs
          .find { x => (x.name == specimenSpec.name) && (x.uniqueId != specimenSpec.uniqueId) }
          .fold
          { true.successNel[DomainError] }
          { _ => DomainError(s"specimen spec name already used: ${specimenSpec.name}").failureNel[Boolean] }
      }
      specValid <- CollectionSpecimenSpec.validate(specimenSpec)
    } yield copy(specimenSpecs = specimenSpecs - specimenSpec + specimenSpec,
                 version       = version + 1,
                 timeModified  = Some(DateTime.now))
  }

  def removeSpecimenSpec(specimenSpecId: String): DomainValidation[CollectionEventType] = {
    specimenSpecs
      .find { x => x.uniqueId == specimenSpecId }
      .fold
      { DomainError(s"specimen spec does not exist: $specimenSpecId").failureNel[CollectionEventType] }
      { specimenSpec =>
        copy(specimenSpecs = specimenSpecs - specimenSpec,
             version       = version + 1,
             timeModified  = Some(DateTime.now)).successNel[DomainError]
      }
  }

  def hasSpecimenSpecs(): Boolean = {
    ! specimenSpecs.isEmpty
  }

  def specimenSpec(uniqueId: String): DomainValidation[CollectionSpecimenSpec] = {
    specimenSpecs.find(_.uniqueId == uniqueId).toSuccessNel("specimen spec not found")
  }

  override def toString: String =
    s"""|CollectionEventType:{
        |  studyId:           $studyId,
        |  id:                $id,
        |  version:           $version,
        |  timeAdded:         $timeAdded,
        |  timeModified:      $timeModified,
        |  name:              $name,
        |  description:       $description,
        |  recurring:         $recurring,
        |  specimenSpecs:     { $specimenSpecs },
        |  annotationTypes:   { $annotationTypes }
        |}""".stripMargin

}

object CollectionEventType extends CollectionEventTypeValidations {
  import org.biobank.domain.CommonValidations._

  implicit val collectionEventTypeWrites: Writes[CollectionEventType] =
    Json.writes[CollectionEventType]

  def create(studyId:            StudyId,
             id:                 CollectionEventTypeId,
             version:            Long,
             name:               String,
             description:        Option[String],
             recurring:          Boolean,
             specimenSpecs:      Set[CollectionSpecimenSpec],
             annotationTypes:    Set[AnnotationType])
      : DomainValidation[CollectionEventType] = {
    (validateId(studyId, StudyIdRequired) |@|
       validateId(id) |@|
       validateVersion(version) |@|
       validateString(name, NameRequired) |@|
       validateNonEmptyOption(description, InvalidDescription) |@|
       specimenSpecs.toList.traverseU(CollectionSpecimenSpec.validate) |@|
       annotationTypes.toList.traverseU(AnnotationType.validate)) {
      case (_, _, _, _, _, _, _) => CollectionEventType(studyId,
                                                        id,
                                                        version,
                                                        DateTime.now,
                                                        None,
                                                        name,
                                                        description,
                                                        recurring,
                                                        specimenSpecs,
                                                        annotationTypes)
    }
  }

}
