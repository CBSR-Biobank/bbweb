package org.biobank.domain.centre

import org.biobank.ValidationKey
import org.biobank.domain._
import org.biobank.domain.study.StudyId
import org.biobank.infrastructure.JsonUtils._

import play.api.libs.json._
import org.joda.time.DateTime
import scalaz.Scalaz._

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
    with HasUniqueName
    with HasDescriptionOption {

  val studyIds: Set[StudyId]

  val locations: Set[Location]

  def locationWithId(locationId: String): DomainValidation[Location] = {
    locations.find(_.uniqueId == locationId)
      .toSuccessNel(s"invalid location id: $locationId")
  }

  def locationName(locationId: String): DomainValidation[String] = {
    locationWithId(locationId).map(loc => s"${this.name}: ${loc.name}")
  }

  override def toString =
    s"""|${this.getClass.getSimpleName}: {
        |  id:           $id,
        |  version:      $version,
        |  timeAdded:    $timeAdded,
        |  timeModified: $timeModified,
        |  name:         $name,
        |  description:  $description,
        |  studyIds:     $studyIds,
        |  locations:    $locations
        |}""".stripMargin

}

object Centre {

  implicit val centreWrites: Writes[Centre] = new Writes[Centre] {
      def writes(centre: Centre) = {
        Json.obj("id"           -> centre.id,
                 "version"      -> centre.version,
                 "timeAdded"    -> centre.timeAdded,
                 "timeModified" -> centre.timeModified,
                 "name"         -> centre.name,
                 "description"  -> centre.description,
                 "studyIds"     -> centre.studyIds,
                 "locations"    -> centre.locations,
                 "status"       -> centre.getClass.getSimpleName)
      }
    }

  def compareByName(a: Centre, b: Centre) = (a.name compareToIgnoreCase b.name) < 0

  def compareByStatus(a: Centre, b: Centre) = {
    val statusCompare = a.getClass.getSimpleName compare b.getClass.getSimpleName
    if (statusCompare == 0) {
      compareByName(a, b)
    } else {
      statusCompare < 0
    }
  }
}

trait CentreValidations {

  val NameMinLength = 2

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
    extends Centre with CentreValidations {
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
  def removeLocation(locationUniqueId: String): DomainValidation[DisabledCentre] = {
    locations.find { x => x.uniqueId == locationUniqueId }.fold {
      DomainError(s"location does not exist: $locationUniqueId")
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
    extends Centre {

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
