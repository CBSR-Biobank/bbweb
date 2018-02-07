package org.biobank.domain.centre

import java.time.OffsetDateTime
import org.biobank.ValidationKey
import org.biobank.dto.NameAndStateDto
import org.biobank.domain._
import org.biobank.domain.study.StudyId
import org.biobank.infrastructure.EnumUtils._
import play.api.libs.json._
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
    with HasSlug
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

  def nameDto(): NameAndStateDto = NameAndStateDto(id.id, slug, name, state.id)

  override def toString: String =
    s"""|${this.getClass.getSimpleName}: {
        |  id:           $id,
        |  version:      $version,
        |  timeAdded:    $timeAdded,
        |  timeModified: $timeModified,
        |  state:        $state
        |  slug:         $slug,
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
                 "slug"      -> centre.slug,
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
                                timeAdded:    OffsetDateTime,
                                timeModified: Option[OffsetDateTime],
                                slug:         String,
                                name:         String,
                                description:  Option[String],
                                studyIds:     Set[StudyId],
                                locations:    Set[Location])
    extends { val state: EntityState = Centre.disabledState }
    with Centre
    with CentreValidations {
  import org.biobank.CommonValidations._
  import org.biobank.domain.DomainValidations._

  /** Used to change the name. */
  def withName(name: String): DomainValidation[DisabledCentre] = {
    validateString(name, NameMinLength, InvalidName) map { _ =>
      copy(name         = name,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now))
    }
  }

  /** Used to change the description. */
  def withDescription(description: Option[String]): DomainValidation[DisabledCentre] = {
    validateNonEmptyStringOption(description, InvalidDescription) map { _ =>
      copy(description  = description,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now))
    }
  }

  /** Associates a study to this centre. Nothing happens if the studyId is already associated. */
  def withStudyId(studyId: StudyId): DomainValidation[DisabledCentre] = {
    validateId(studyId, InvalidStudyId).map { _ =>
      copy(studyIds     = studyIds + studyId,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now))

    }
  }

  /** Removes a study from this centre. */
  def removeStudyId(studyId: StudyId): DomainValidation[DisabledCentre] = {
    studyIds.find { x => x == studyId }.fold {
      DomainError(s"studyId not associated with centre: $studyId").failureNel[DisabledCentre]
    } { _ =>
      copy(studyIds     = studyIds - studyId,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now)).successNel[String]
    }
  }

  /** adds a location to this centre. */
  def withLocation(location: Location): DomainValidation[DisabledCentre] = {
    checkAddLocation(location).map { _ =>
      // replaces previous location with same unique id
      val newLocations = locations - location + location
      copy(locations    = newLocations,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now))
    }
  }

  /** removes a location from this centre. */
  def removeLocation(id: LocationId): DomainValidation[DisabledCentre] = {
    checkRemoveLocation(id).map { location =>
      val newLocations = locations - location
      copy(locations    = newLocations,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now))
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
                    slug         = this.slug,
                    name         = this.name,
                    description  = this.description,
                    studyIds     = this.studyIds,
                    locations    = this.locations).successNel[String]
    }
  }

  protected def checkAddLocation(location: Location): DomainValidation[Boolean] = {
    (Location.validate(location) |@| nameNotUsed(location)) {
      case _ => true
    }
  }

  protected def checkRemoveLocation(locationId: LocationId)
      : DomainValidation[Location] = {
    locations
      .find { x => x.id == locationId }
      .toSuccessNel(s"location does not exist: $locationId")
  }

  protected def nameNotUsed(location: Location): DomainValidation[Boolean] = {
    val nameLowerCase = location.name.toLowerCase
    locations
      .find { x => (x.name.toLowerCase == nameLowerCase) && (x.id != location.id)  }
      match {
        case Some(_) =>
          EntityCriteriaError(s"location name already used: ${location.name}").failureNel[Boolean]
        case None =>
          true.successNel[DomainError]
      }
  }
}

/**
  * Factory object used to create a centre.
  */
object DisabledCentre extends CentreValidations {
  import org.biobank.CommonValidations._
  import org.biobank.domain.DomainValidations._

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
       validateNonEmptyStringOption(description, InvalidDescription) |@|
       studyIds.toList.traverseU(validateStudyId) |@|
       locations.toList.traverseU(Location.validate)) { case _ =>
        DisabledCentre(id           = id,
                       version      = version,
                       timeAdded    = OffsetDateTime.now,
                       timeModified = None,
                       slug         = Slug(name),
                       name         = name,
                       description  = description,
                       studyIds     = studyIds,
                       locations    = locations)
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
                               timeAdded:    OffsetDateTime,
                               timeModified: Option[OffsetDateTime],
                               slug:         String,
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
                   slug         = this.slug,
                   name         = this.name,
                   description  = this.description,
                   studyIds     = this.studyIds,
                   locations    = this.locations).successNel[String]
  }
}
