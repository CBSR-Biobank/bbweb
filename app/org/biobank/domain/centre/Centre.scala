package org.biobank.domain.centre

import org.biobank.ValidationKey
import org.biobank.infrastructure.JsonUtils._
import org.biobank.dto.NameDto
import org.biobank.domain._
import org.biobank.domain.study.StudyId
import org.biobank.infrastructure.EnumUtils._

import play.api.libs.json._
import org.joda.time.DateTime
import scalaz.Scalaz._

/**
 * Predicates that can be used to filter collections of centres.
 *
 */
trait CentrePredicates extends HasNamePredicates[Centre] {

  type CentreFilter = Centre => Boolean

}

/**
  * A Centre can be one or a combination of the following:
  *
  * - Collection Centre: where specimens are collected from patients (e.g. a clinic).
  *
  * - Processing Centre: where collected specimens are processed.
  *
  * - Storage Centre: where collected and processed specimens are stored. Usually, a storage centre is also a
  *   processing centre.
  *
  * - Request Centre: where collected and processed specimens are sent for analysis. Usually the specimens are
  *   stored first at a storage centre for a period of time.
  *
  */
sealed trait Centre
    extends ConcurrencySafeEntity[CentreId]
    with HasState
    with HasUniqueName
    with HasOptionalDescription {

  val state: EntityState

  val studyIds: Set[StudyId]

  val locations: Set[Location]

  def locationWithId(locationId: LocationId): DomainValidation[Location] = {
    locations.find(_.id == locationId)
      .toSuccessNel(s"invalid location id: $locationId")
  }

  def locationName(locationId: LocationId): DomainValidation[String] = {
    locationWithId(locationId).map(loc => s"${this.name}: ${loc.name}")
  }

  def nameDto(): NameDto = NameDto(id.id, name, state.id)

  override def toString: String =
    s"""|${this.getClass.getSimpleName}: {
        |  id:           $id,
        |  version:      $version,
        |  timeAdded:    $timeAdded,
        |  timeModified: $timeModified,
        |  state:        $state
        |  name:         $name,
        |  description:  $description,
        |  studyIds:     $studyIds,
        |  locations:    $locations
        |}""".stripMargin

}

object Centre {
  import org.biobank.domain.Location._

  val disabledState: EntityState = new EntityState("disabled")
  val enabledState: EntityState = new EntityState("enabled")

  val centreStates: List[EntityState] = List(disabledState, enabledState)

  @SuppressWarnings(Array("org.wartremover.warts.Option2Iterable"))
  implicit val centreFormat: Format[Centre] = new Format[Centre] {
      override def writes(centre: Centre): JsValue = {
        ConcurrencySafeEntity.toJson(centre) ++
        Json.obj("state"     -> centre.state.id,
                 "name"      -> centre.name,
                 "studyIds"  -> centre.studyIds,
                 "locations" -> centre.locations) ++
        JsObject(
          Seq[(String, JsValue)]() ++
            centre.description.map("description" -> Json.toJson(_)))
      }

      override def reads(json: JsValue): JsResult[Centre] = (json \ "state") match {
          case JsDefined(JsString(disabledState.id)) => json.validate[DisabledCentre]
          case JsDefined(JsString(enabledState.id))  => json.validate[EnabledCentre]
          case _ => JsError("error")
        }
    }

  implicit val disabledCentreReads: Reads[DisabledCentre] = Json.reads[DisabledCentre]
  implicit val enabledCentreReads: Reads[EnabledCentre]   = Json.reads[EnabledCentre]

  type CentresCompare = (Centre, Centre) => Boolean

  val sort2Compare: Map[String, CentresCompare] = Map[String, CentresCompare](
      "name"  -> Centre.compareByName,
      "state" -> Centre.compareByState
    )

  def compareByName(a: Centre, b: Centre): Boolean =
    (a.name compareToIgnoreCase b.name) < 0

  def compareByState(a: Centre, b: Centre): Boolean = {
    val statusCompare = a.state.id compare b.state.id
    if (statusCompare == 0) compareByName(a, b)
    else statusCompare < 0
  }
}

trait CentreValidations {

  val NameMinLength: Long = 2L

  case object InvalidName extends ValidationKey

  case object InvalidStudyId extends ValidationKey
}


/**
  * This is the initial state for a centre.  In this state, only configuration changes are allowed. Collection
  * and processing of specimens cannot be recorded.
  *
  * This class has a private constructor and instances of this class can only be created using the
  * [[DisabledCentre.create]] method on the factory object.
  */
final case class DisabledCentre(id:           CentreId,
                                version:      Long,
                                timeAdded:    DateTime,
                                timeModified: Option[DateTime],
                                name:         String,
                                description:  Option[String],
                                studyIds:     Set[StudyId],
                                locations:    Set[Location])
    extends { val state: EntityState = Centre.disabledState }
    with Centre
    with CentreValidations {
  import org.biobank.CommonValidations._
  import org.biobank.domain.CommonValidations._

  /** Used to change the name. */
  def withName(name: String): DomainValidation[DisabledCentre] = {
    validateString(name, NameMinLength, InvalidName) map { _ =>
      copy(name         = name,
           version      = version + 1,
           timeModified = Some(DateTime.now))
    }
  }

  /** Used to change the description. */
  def withDescription(description: Option[String]): DomainValidation[DisabledCentre] = {
    validateNonEmptyOption(description, InvalidDescription) map { _ =>
      copy(description  = description,
           version      = version + 1,
           timeModified = Some(DateTime.now))
    }
  }

  /** Associates a study to this centre. Nothing happens if the studyId is already associated. */
  def withStudyId(studyId: StudyId): DomainValidation[DisabledCentre] = {
    validateId(studyId, InvalidStudyId).map { _ =>
      copy(studyIds     = studyIds + studyId,
           version      = version + 1,
           timeModified = Some(DateTime.now))

    }
  }

  /** Removes a study from this centre. */
  def removeStudyId(studyId: StudyId): DomainValidation[DisabledCentre] = {
    studyIds.find { x => x == studyId }.fold {
      DomainError(s"studyId not associated with centre: $studyId").failureNel[DisabledCentre]
    } { _ =>
      copy(studyIds     = studyIds - studyId,
           version      = version + 1,
           timeModified = Some(DateTime.now)).successNel[String]
    }
  }

  /** adds a location to this centre. */
  def withLocation(location: Location): DomainValidation[DisabledCentre] = {
    Location.validate(location).map { _ =>
      // replaces previous annotation type with same unique id
      copy(locations    = locations - location + location,
           version      = version + 1,
           timeModified = Some(DateTime.now))
    }
  }

  /** removes a location from this centre. */
  def removeLocation(id: LocationId): DomainValidation[DisabledCentre] = {
    locations.find { x => x.id == id }.fold {
      DomainError(s"location does not exist: $id")
        .failureNel[DisabledCentre]
    } { location =>
      copy(locations    = locations - location,
           version      = version + 1,
           timeModified = Some(DateTime.now)).successNel[String]
    }
  }

  /** Used to enable a centre after it has been configured, or had configuration changes made on it. */
  def enable(): DomainValidation[EnabledCentre] = {
    if (this.locations.size <= 0) {
      EntityCriteriaError("centre does not have locations").failureNel[EnabledCentre]
    } else {
      EnabledCentre(id           = this.id,
                    version      = this.version + 1,
                    timeAdded    = this.timeAdded,
                    timeModified = this.timeModified,
                    name         = this.name,
                    description  = this.description,
                    studyIds     = this.studyIds,
                    locations    = this.locations).successNel[String]
    }
  }
}

/**
  * Factory object used to create a centre.
  */
object DisabledCentre extends CentreValidations {
  import org.biobank.domain.CommonValidations._

  /**
    * The factory method to create a centre.
    *
    * Performs validation on fields.
    */
  def create(id:          CentreId,
             version:     Long,
             name:        String,
             description: Option[String],
             studyIds:    Set[StudyId],
             locations:   Set[Location]): DomainValidation[DisabledCentre] = {

    def validateStudyId(studyId: StudyId) = validateId(studyId, InvalidStudyId)

    (validateId(id) |@|
       validateVersion(version) |@|
       validateString(name, NameMinLength, InvalidName) |@|
       validateNonEmptyOption(description, InvalidDescription) |@|
       studyIds.toList.traverseU(validateStudyId) |@|
       locations.toList.traverseU(Location.validate)) {
      case (_, validVersion, _, _, _, _) =>
        DisabledCentre(id, validVersion, DateTime.now, None, name, description, studyIds, locations)
    }
  }
}

/**
  * When a centre is in this state, collection and processing of specimens can be recorded.
  *
  * This class has a private constructor and instances of this class can only be created using
  * the [[EnabledCentre.create]] method on the factory object.
  */
final case class EnabledCentre(id:           CentreId,
                               version:      Long,
                               timeAdded:    DateTime,
                               timeModified: Option[DateTime],
                               name:         String,
                               description:  Option[String],
                               studyIds:     Set[StudyId],
                               locations:    Set[Location])
    extends { val state: EntityState = Centre.enabledState }
    with Centre {

  def disable(): DomainValidation[DisabledCentre] = {
    DisabledCentre(id           = this.id,
                   version      = this.version + 1,
                   timeAdded    = this.timeAdded,
                   timeModified = this.timeModified,
                   name         = this.name,
                   description  = this.description,
                   studyIds     = this.studyIds,
                   locations    = this.locations).successNel[String]
  }
}
