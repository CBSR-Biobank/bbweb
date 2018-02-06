package org.biobank.domain.study

import java.time.OffsetDateTime
import org.biobank.ValidationKey
import org.biobank.domain._
import org.biobank.infrastructure.EnumUtils._
import play.api.libs.json._
import scalaz.Scalaz._

/**
 * Predicates that can be used to filter collections of studies.
 *
 */
trait StudyPredicates extends HasNamePredicates[Study] {

  type StudyFilter = Study => Boolean

}

/** A Study represents a collection of participants and specimens collected for a particular research
 * study. This is an aggregate root.
 *
 * A study can be in one of 3 states: diabled, enabled, or retired. These are represented by
 * the sub classes.
 *
 */
sealed trait Study
    extends ConcurrencySafeEntity[StudyId]
    with HasState
    with HasUniqueName
    with HasSlug
    with HasOptionalDescription {

  val state: EntityState

  /** The annotation types associated with participants of this study. */
  val annotationTypes: Set[AnnotationType]

  override def toString: String =
    s"""|${this.getClass.getSimpleName}: {
        |  id:              $id,
        |  version:         $version,
        |  timeAdded:       $timeAdded,
        |  timeModified:    $timeModified,
        |  state:           $state,
        |  slug:            $slug,
        |  name:            $name,
        |  description:     $description,
        |  annotationTypes: $annotationTypes
        |}""".stripMargin

}

object Study {

  val disabledState: EntityState = new EntityState("disabled")
  val enabledState: EntityState  = new EntityState("enabled")
  val retiredState: EntityState  = new EntityState("retired")

  val studyStates: List[EntityState] = List(disabledState,
                                            enabledState,
                                            retiredState)


  val sort2Compare: Map[String, (Study, Study) => Boolean] =
    Map[String, (Study, Study) => Boolean](
      "name"  -> compareByName,
      "state" -> compareByState)

  @SuppressWarnings(Array("org.wartremover.warts.Option2Iterable"))
  implicit val studyFormat: Format[Study] = new Format[Study] {
      override def writes(study: Study): JsValue = {
        ConcurrencySafeEntity.toJson(study) ++
        Json.obj("state"           -> study.state.id,
                 "slug"            -> study.slug,
                 "name"            -> study.name,
                 "annotationTypes" -> study.annotationTypes) ++
        JsObject(
          Seq[(String, JsValue)]() ++
            study.description.map("description" -> Json.toJson(_)))
      }

      override def reads(json: JsValue): JsResult[Study] = (json \ "state") match {
          case JsDefined(JsString(disabledState.id)) => json.validate[DisabledStudy]
          case JsDefined(JsString(enabledState.id))  => json.validate[EnabledStudy]
          case JsDefined(JsString(retiredState.id))  => json.validate[RetiredStudy]
          case _ => JsError("error")
        }
    }

  implicit val disabledStudyReads: Reads[DisabledStudy] = Json.reads[DisabledStudy]
  implicit val enabledStudyReads: Reads[EnabledStudy]   = Json.reads[EnabledStudy]
  implicit val retiredStudyReads: Reads[RetiredStudy]   = Json.reads[RetiredStudy]

  def compareByName(a: Study, b: Study): Boolean =
    (a.name compareToIgnoreCase b.name) < 0

  def compareByState(a: Study, b: Study): Boolean = {
    val stateCompare = a.state.id compare b.state.id
    if (stateCompare == 0) compareByName(a, b)
    else stateCompare < 0
  }
}

trait StudyValidations {

  val NameMinLength: Long = 2L

  case object InvalidSpecimenGroupId extends ValidationKey
}

/**
 * This is the initial state for a study.  In this state, only configuration changes are allowed.
 * Collection and processing of specimens cannot be recorded.
 *
 * This class has a private constructor and instances of this class can only be created using
 * the [[DisabledStudy.create]] method on the factory object.
 *
 * NOTE: functions withName and withDescription should be moved to the Study trait after this
 *       bug is fixed: https://issues.scala-lang.org/browse/SI-5122
 */
final case class DisabledStudy(id:              StudyId,
                               version:         Long,
                               timeAdded:       OffsetDateTime,
                               timeModified:    Option[OffsetDateTime],
                               slug:            String,
                               name:            String,
                               description:     Option[String],
                               annotationTypes: Set[AnnotationType])
    extends { val state: EntityState = Study.disabledState }
    with Study
    with StudyValidations
    with AnnotationTypeValidations
    with HasAnnotationTypes
    with HasSlug {
  import CommonValidations._

  /** Used to change the name. */
  def withName(name: String): DomainValidation[DisabledStudy] = {
    validateString(name, NameMinLength, InvalidName) map { _ =>
      copy(slug         = Slug(name),
           name         = name,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now))
    }
  }

  /** Used to change the description. */
  def withDescription(description: Option[String]): DomainValidation[DisabledStudy] = {
    validateNonEmptyOption(description, InvalidDescription) map { _ =>
      copy(description  = description,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now))
    }
  }

  /** adds a participant annotation type to this study. */
  def withParticipantAnnotationType(annotationType: AnnotationType)
      : DomainValidation[DisabledStudy] = {
    checkAddAnnotationType(annotationType).map { _ =>
      // replaces previous annotation type with same unique id
      val newAnnotationTypes = annotationTypes - annotationType + annotationType
      copy(annotationTypes = newAnnotationTypes,
           version         = version + 1,
           timeModified    = Some(OffsetDateTime.now))
    }
  }

  /** removes a participant annotation type from this study. */
  def removeParticipantAnnotationType(annotationTypeId: AnnotationTypeId): DomainValidation[DisabledStudy] = {
    checkRemoveAnnotationType(annotationTypeId).map { annotationType =>
      val newAnnotationTypes = annotationTypes - annotationType
      copy(annotationTypes = newAnnotationTypes,
           version         = version + 1,
           timeModified    = Some(OffsetDateTime.now))
    }
  }

  /** Used to enable a study after it has been configured, or had configuration changes made on it. */
  def enable(): DomainValidation[EnabledStudy] = {
    EnabledStudy(id              = this.id,
                 version         = this.version + 1,
                 timeAdded       = this.timeAdded,
                 timeModified    = Some(OffsetDateTime.now),
                 slug            = this.slug,
                 name            = this.name,
                 description     = this.description,
                 annotationTypes = this.annotationTypes).successNel[String]
  }

  /** When a study will no longer collect specimens from participants it can be retired. */
  def retire(): DomainValidation[RetiredStudy] = {
    RetiredStudy(id              = this.id,
                 version         = this.version + 1,
                 timeAdded       = this.timeAdded,
                 timeModified    = Some(OffsetDateTime.now),
                 slug            = this.slug,
                 name            = this.name,
                 description     = this.description,
                 annotationTypes = this.annotationTypes).successNel[String]
  }

}

/**
 * Factory object used to create a study.
 */
object DisabledStudy extends StudyValidations with AnnotationTypeValidations {
  import CommonValidations._

  /**
   * The factory method to create a study.
   *
   * Performs validation on fields.
   */
  def create(id:              StudyId,
             version:         Long,
             name:            String,
             description:     Option[String],
             annotationTypes: Set[AnnotationType])
      : DomainValidation[DisabledStudy] = {
    (validateId(id) |@|
       validateVersion(version) |@|
       validateString(name, NameMinLength, InvalidName) |@|
       validateNonEmptyOption(description, InvalidDescription) |@|
       annotationTypes.toList.traverseU(validate)) { case _ =>
        DisabledStudy(id              = id,
                      version         = version,
                      timeAdded       = OffsetDateTime.now,
                      timeModified    = None,
                      slug            = Slug(name),
                      name            = name,
                      description     = description,
                      annotationTypes = annotationTypes)
    }
  }

}

/**
 * When a study is in this state, collection and processing of specimens can be recorded.
 *
 * This class has a private constructor and instances of this class can only be created using
 * the [[EnabledStudy.create]] method on the factory object.
 */
final case class EnabledStudy(id:              StudyId,
                              version:         Long,
                              timeAdded:       OffsetDateTime,
                              timeModified:    Option[OffsetDateTime],
                              slug:            String,
                              name:            String,
                              description:     Option[String],
                              annotationTypes: Set[AnnotationType])
    extends { val state: EntityState = Study.enabledState }
    with Study {

  def disable(): DomainValidation[DisabledStudy] = {
    DisabledStudy(id              = this.id,
                  version         = this.version + 1,
                  timeAdded       = this.timeAdded,
                  timeModified    = Some(OffsetDateTime.now),
                  slug            = this.slug,
                  name            = this.name,
                  description     = this.description,
                  annotationTypes = this.annotationTypes).successNel[String]
  }
}

/**
 *  In this state the study cannot be modified and collection and processing of specimens is not allowed.
 *
 * This class has a private constructor and instances of this class can only be created using
 * the [[RetiredStudy.create]] method on the factory object.
 */
final case class RetiredStudy(id:              StudyId,
                              version:         Long,
                              timeAdded:       OffsetDateTime,
                              timeModified:    Option[OffsetDateTime],
                              slug:            String,
                              name:            String,
                              description:     Option[String],
                              annotationTypes: Set[AnnotationType])
    extends { val state: EntityState = Study.retiredState }
    with Study {

  def unretire(): DomainValidation[DisabledStudy] = {
    DisabledStudy(id              = this.id,
                  version         = this.version + 1,
                  timeAdded       = this.timeAdded,
                  timeModified    = Some(OffsetDateTime.now),
                  slug            = this.slug,
                  name            = this.name,
                  description     = this.description,
                  annotationTypes = this.annotationTypes).successNel[String]
  }
}
