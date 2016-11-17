package org.biobank.domain.study

import org.biobank.ValidationKey
import org.biobank.domain._
import org.biobank.infrastructure.EnumUtils._
import play.api.libs.json._
import org.joda.time.DateTime
import scalaz.Scalaz._

/**
 * Possible states for a study.
 */
@SuppressWarnings(Array("org.wartremover.warts.Enumeration"))
object StudyState extends Enumeration {
  type StudyState = Value
  val Disabled = Value("disabled")
  val Enabled  = Value("enabled")
  val Retired  = Value("retired")

  implicit val studyStateFormat: Format[StudyState] = enumFormat(StudyState)

}

/**
 * Predicates that can be used to filter collections of studies.
 *
 */
trait StudyPredicates {
  import StudyState._

  type StudyFilter = Study => Boolean

  val nameIsOneOf: Set[String] => StudyFilter =
    names => study => names.contains(study.name)

  val stateIsOneOf: Set[StudyState] => StudyFilter =
    states => study => states.contains(study.state)

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
    with HasUniqueName
    with HasDescriptionOption {
  import StudyState._

  val state: StudyState

  /** The annotation types associated with participants of this study. */
  val annotationTypes: Set[AnnotationType]

  override def toString =
    s"""|${this.getClass.getSimpleName}: {
        |  id:              $id,
        |  version:         $version,
        |  timeAdded:       $timeAdded,
        |  timeModified:    $timeModified,
        |  name:            $name,
        |  description:     $description
        |  annotationTypes: $annotationTypes
        |  state:           $state
        |}""".stripMargin

}

object Study {

  val sort2Compare = Map[String, (Study, Study) => Boolean](
      "name"  -> compareByName,
      "state" -> compareByState)

  @SuppressWarnings(Array("org.wartremover.warts.Option2Iterable"))
  implicit val studyWrites: Writes[Study] = new Writes[Study] {
      def writes(study: Study) = {
        ConcurrencySafeEntity.toJson(study) ++
        Json.obj("name"            -> study.name,
                 "annotationTypes" -> study.annotationTypes,
                 "state"           -> study.state) ++
        JsObject(
          Seq[(String, JsValue)]() ++
            study.description.map("description" -> Json.toJson(_)))
      }

    }

  def compareByName(a: Study, b: Study) = (a.name compareToIgnoreCase b.name) < 0

  def compareByState(a: Study, b: Study) = {
    val stateCompare = a.state compare b.state
    if (stateCompare == 0) compareByName(a, b)
    else stateCompare < 0
  }
}

trait StudyValidations {

  val NameMinLength = 2L

  case object InvalidStudyId extends ValidationKey

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
final case class DisabledStudy(id:                         StudyId,
                               version:                    Long,
                               timeAdded:                  DateTime,
                               timeModified:               Option[DateTime],
                               name:                       String,
                               description:                Option[String],
                               annotationTypes: Set[AnnotationType])
    extends { val state = StudyState.Disabled }
    with Study
    with StudyValidations
    with AnnotationTypeValidations
    with HasAnnotationTypes {
  import CommonValidations._

  /** Used to change the name. */
  def withName(name: String): DomainValidation[DisabledStudy] = {
    validateString(name, NameMinLength, InvalidName) map { _ =>
      copy(name         = name,
           version      = version + 1,
           timeModified = Some(DateTime.now))
    }
  }

  /** Used to change the description. */
  def withDescription(description: Option[String]): DomainValidation[DisabledStudy] = {
    validateNonEmptyOption(description, InvalidDescription) map { _ =>
      copy(description  = description,
           version      = version + 1,
           timeModified = Some(DateTime.now))
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
           timeModified    = Some(DateTime.now))
    }
  }

  /** removes a participant annotation type from this study. */
  def removeParticipantAnnotationType(annotationTypeId: String)
      : DomainValidation[DisabledStudy] = {
    checkRemoveAnnotationType(annotationTypeId).map { annotationType =>
      val newAnnotationTypes = annotationTypes - annotationType
      copy(annotationTypes = newAnnotationTypes,
           version         = version + 1,
           timeModified    = Some(DateTime.now))
    }
  }

  /** Used to enable a study after it has been configured, or had configuration changes made on it. */
  def enable(): DomainValidation[EnabledStudy] = {
    EnabledStudy(id              = this.id,
                 version         = this.version + 1,
                 timeAdded       = this.timeAdded,
                 timeModified    = Some(DateTime.now),
                 name            = this.name,
                 description     = this.description,
                 annotationTypes = this.annotationTypes).successNel[String]
  }

  /** When a study will no longer collect specimens from participants it can be retired. */
  def retire(): DomainValidation[RetiredStudy] = {
    RetiredStudy(id              = this.id,
                 version         = this.version + 1,
                 timeAdded       = this.timeAdded,
                 timeModified    = Some(DateTime.now),
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
       annotationTypes.toList.traverseU(validate)) {
      case (_, newVersion, _, _, _) =>
        DisabledStudy(id, newVersion, DateTime.now, None, name, description, annotationTypes)
    }
  }

}

/**
 * When a study is in this state, collection and processing of specimens can be recorded.
 *
 * This class has a private constructor and instances of this class can only be created using
 * the [[EnabledStudy.create]] method on the factory object.
 */
final case class EnabledStudy(id:                         StudyId,
                              version:                    Long,
                              timeAdded:                  DateTime,
                              timeModified:               Option[DateTime],
                              name:                       String,
                              description:                Option[String],
                              annotationTypes: Set[AnnotationType])
    extends { val state = StudyState.Enabled }
    with Study {

  def disable(): DomainValidation[DisabledStudy] = {
    DisabledStudy(id              = this.id,
                  version         = this.version + 1,
                  timeAdded       = this.timeAdded,
                  timeModified    = Some(DateTime.now),
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
final case class RetiredStudy(id:                         StudyId,
                              version:                    Long,
                              timeAdded:                  DateTime,
                              timeModified:               Option[DateTime],
                              name:                       String,
                              description:                Option[String],
                              annotationTypes: Set[AnnotationType])
    extends { val state = StudyState.Retired }
    with Study {

  def unretire(): DomainValidation[DisabledStudy] = {
    DisabledStudy(id              = this.id,
                  version         = this.version + 1,
                  timeAdded       = this.timeAdded,
                  timeModified    = Some(DateTime.now),
                  name            = this.name,
                  description     = this.description,
                  annotationTypes = this.annotationTypes).successNel[String]
  }
}
