package org.biobank.domain.studies

import java.time.OffsetDateTime
import org.biobank.ValidationKey
import org.biobank.domain._
import org.biobank.domain.annotations._
import play.api.libs.json._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

/**
 * Predicates that can be used to filter collections of [[CollectionEventType]]s.
 *
 */
trait CollectionEventTypePredicates extends HasNamePredicates[CollectionEventType] {

  type CollectionEventTypeFilter = CollectionEventType => Boolean

}

trait CollectionEventTypeValidations {

  case object MaxCountInvalid extends ValidationKey

  case object AmountInvalid extends ValidationKey

  case object StudyIdRequired extends ValidationKey
}

/**
  * Defines a classification name, unique to the Study, to a participant visit.
  *
  * A participant visit is a record of when specimens were collected from a
  * [[domain.participants.Participant]] at a collection [[domain.centres.Centre]]. Each collection event type
  * is assigned one or more [[domain.studies.CollectionSpecimenDefinition CollectionSpecimenDefinitions]] to
  * specify the type of [[domain.participants.Specimen Specimens]] that are collected.
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
final case class CollectionEventType(studyId:              StudyId,
                                     id:                   CollectionEventTypeId,
                                     version:              Long,
                                     timeAdded:            OffsetDateTime,
                                     timeModified:         Option[OffsetDateTime],
                                     slug:                 String,
                                     name:                 String,
                                     description:          Option[String],
                                     recurring:            Boolean,
                                     specimenDefinitions: Set[CollectionSpecimenDefinition],
                                     annotationTypes:      Set[AnnotationType])
    extends ConcurrencySafeEntity[CollectionEventTypeId]
    with HasName
    with HasSlug
    with HasOptionalDescription
    with HasStudyId
    with HasAnnotationTypes {
  import org.biobank.CommonValidations._
  import org.biobank.domain.DomainValidations._

  def withName(name: String): DomainValidation[CollectionEventType] = {
    validateString(name, NameRequired).map { _ =>
      copy(name         = name,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now))
    }
  }

  def withDescription(description: Option[String]): DomainValidation[CollectionEventType] = {
    validateNonEmptyStringOption(description, InvalidDescription) map { _ =>
      copy(description  = description,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now))
    }
  }

  def withRecurring(recurring: Boolean): DomainValidation[CollectionEventType] = {
    copy(recurring   = recurring,
         version      = version + 1,
         timeModified = Some(OffsetDateTime.now)).successNel[String]
  }

  def withAnnotationType(annotationType: AnnotationType)
      : DomainValidation[CollectionEventType] = {
    checkAddAnnotationType(annotationType).map { _ =>
      // replaces previous annotation type with same unique id
      val newAnnotationTypes = annotationTypes - annotationType + annotationType
      copy(annotationTypes = newAnnotationTypes,
           version         = version + 1,
           timeModified    = Some(OffsetDateTime.now))
    }
  }

  def removeAnnotationType(annotationTypeId: AnnotationTypeId): DomainValidation[CollectionEventType] = {
    checkRemoveAnnotationType(annotationTypeId).map { annotationType =>
      val newAnnotationTypes = annotationTypes - annotationType
      copy(annotationTypes = newAnnotationTypes,
           version         = version + 1,
           timeModified    = Some(OffsetDateTime.now))
    }
  }

  // replaces a previous one with the same unique id if it exists
  //
  // fails if there is another with the same name
  def withSpecimenDefinition(specimenDesc: CollectionSpecimenDefinition)
      : DomainValidation[CollectionEventType] = {
    for {
      nameNotUsed <- {
        specimenDefinitions
          .find { x => (x.name == specimenDesc.name) && (x.id != specimenDesc.id) }
          .fold
          { true.successNel[DomainError] }
          { _ => DomainError(s"specimen spec name already used: ${specimenDesc.name}").failureNel[Boolean] }
      }
      specValid <- CollectionSpecimenDefinition.validate(specimenDesc)
    } yield copy(specimenDefinitions = specimenDefinitions - specimenDesc + specimenDesc,
                 version       = version + 1,
                 timeModified  = Some(OffsetDateTime.now))
  }

  def removeSpecimenDefinition(specimenDescId: SpecimenDefinitionId)
      : DomainValidation[CollectionEventType] = {
    specimenDefinitions
      .find { x => x.id == specimenDescId }
      .fold
      { DomainError(s"specimen spec does not exist: $specimenDescId").failureNel[CollectionEventType] }
      { specimenDesc =>
        copy(specimenDefinitions = specimenDefinitions - specimenDesc,
             version       = version + 1,
             timeModified  = Some(OffsetDateTime.now)).successNel[DomainError]
      }
  }

  def hasSpecimenDefinitions(): Boolean = {
    ! specimenDefinitions.isEmpty
  }

  def specimenDesc(id: SpecimenDefinitionId): DomainValidation[CollectionSpecimenDefinition] = {
    specimenDefinitions.find(_.id == id).toSuccessNel("specimen description not found")
  }

  override def toString: String =
    s"""|CollectionEventType:{
        |  studyId:              $studyId,
        |  id:                   $id,
        |  version:              $version,
        |  timeAdded:            $timeAdded,
        |  timeModified:         $timeModified,
        |  slug:                 $slug,
        |  name:                 $name,
        |  description:          $description,
        |  recurring:            $recurring,
        |  specimenDefinitions: { $specimenDefinitions },
        |  annotationTypes:      { $annotationTypes }
        |}""".stripMargin

}

object CollectionEventType extends CollectionEventTypeValidations {
  import org.biobank.CommonValidations._
  import org.biobank.domain.DomainValidations._

  implicit val collectionEventTypeWrites: Format[CollectionEventType] = Json.format[CollectionEventType]

  def create(studyId:              StudyId,
             id:                   CollectionEventTypeId,
             version:              Long,
             name:                 String,
             description:          Option[String],
             recurring:            Boolean,
             specimenDefinitions: Set[CollectionSpecimenDefinition],
             annotationTypes:      Set[AnnotationType])
      : DomainValidation[CollectionEventType] = {
    (validateId(studyId, StudyIdRequired) |@|
       validateId(id) |@|
       validateVersion(version) |@|
       validateNonEmptyString(name, NameRequired) |@|
       validateNonEmptyStringOption(description, InvalidDescription) |@|
       specimenDefinitions.toList.traverseU(CollectionSpecimenDefinition.validate) |@|
       annotationTypes.toList.traverseU(AnnotationType.validate)) { case _ =>
        CollectionEventType(studyId              = studyId,
                            id                   = id,
                            version              = version,
                            timeAdded            = OffsetDateTime.now,
                            timeModified         = None,
                            slug                 = Slug(name),
                            name                 = name,
                            description          = description,
                            recurring            = recurring,
                            specimenDefinitions = specimenDefinitions,
                            annotationTypes      = annotationTypes)
    }
  }

  val sort2Compare: Map[String, (CollectionEventType, CollectionEventType) => Boolean] =
    Map[String, (CollectionEventType, CollectionEventType) => Boolean]("name"  -> compareByName)

  def compareByName(a: CollectionEventType, b: CollectionEventType): Boolean =
    (a.name compareToIgnoreCase b.name) < 0

}
