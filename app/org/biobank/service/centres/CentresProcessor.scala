package org.biobank.service.centres

import akka.actor._
import akka.persistence.{ RecoveryCompleted, SnapshotOffer }
import javax.inject.{Inject}
import org.biobank.TestData
import org.biobank.domain.centre._
import org.biobank.domain.study.{StudyId, StudyRepository}
import org.biobank.domain.{DomainValidation, Location}
import org.biobank.infrastructure.command.CentreCommands._
import org.biobank.infrastructure.event.CentreEvents._
import org.biobank.service.Processor
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

object CentresProcessor {

  def props = Props[CentresProcessor]

}

class CentresProcessor @Inject() (val centreRepository: CentreRepository,
                                  val studyRepository:  StudyRepository,
                                  val testData:         TestData)
    extends Processor {
  import org.biobank.CommonValidations._
  import CentreEvent.EventType

  override def persistenceId = "centre-processor-id"

  case class SnapshotState(centres: Set[Centre])

  val ErrMsgNameExists = "centre with name already exists"

  val receiveRecover: Receive = {
    case event: CentreEvent => event.eventType match {
        case et: EventType.Added              => applyAddedEvent(event)
        case et: EventType.NameUpdated        => applyNameUpdatedEvent(event)
        case et: EventType.DescriptionUpdated => applyDescriptionUpdatedEvent(event)
        case et: EventType.Enabled            => applyEnabledEvent(event)
        case et: EventType.Disabled           => applyDisabledEvent(event)
        case et: EventType.LocationAdded      => applyLocationAddedEvent(event)
        case et: EventType.LocationUpdated    => applyLocationUpdatedEvent(event)
        case et: EventType.LocationRemoved    => applyLocationRemovedEvent(event)
        case et: EventType.StudyAdded         => applyStudyAddedEvent(event)
        case et: EventType.StudyRemoved       => applyRemovedFromStudyEvent(event)

        case et => log.error(s"event not handled: $event")
      }

    case SnapshotOffer(_, snapshot: SnapshotState) =>
      snapshot.centres.foreach{ centre => centreRepository.put(centre) }

    case event: RecoveryCompleted => // silence these messages

    case cmd => log.error(s"CentresProcessor: message not handled: $cmd")
  }

  val receiveCommand: Receive = {
    case cmd: AddCentreCmd               => processAddCentreCmd(cmd)
    case cmd: UpdateCentreNameCmd        => processUpdateCentreNameCmd(cmd)
    case cmd: UpdateCentreDescriptionCmd => processUpdateCentreDescriptionCmd(cmd)
    case cmd: EnableCentreCmd            => processEnableCentreCmd(cmd)
    case cmd: DisableCentreCmd           => processDisableCentreCmd(cmd)
    case cmd: AddCentreLocationCmd       => processAddLocationCmd(cmd)
    case cmd: UpdateCentreLocationCmd    => processUpdateLocationCmd(cmd)
    case cmd: RemoveCentreLocationCmd    => processRemoveLocationCmd(cmd)
    case cmd: AddStudyToCentreCmd        => processAddStudyToCmd(cmd)
    case cmd: RemoveStudyFromCentreCmd   => processRemoveStudyCmd(cmd)

    case "snap" =>
      saveSnapshot(SnapshotState(centreRepository.getValues.toSet))
      stash()

    case cmd => log.error(s"CentresProcessor: message not handled: $cmd")
  }

  private def processAddCentreCmd(cmd: AddCentreCmd): Unit = {
    val centreId = centreRepository.nextIdentity

    if (centreRepository.getByKey(centreId).isSuccess) {
      log.error(s"centre with id already exsits: $centreId")
    }

    val event = for {
        nameAvailable <- nameAvailable(cmd.name)
        newCentre <- DisabledCentre.create(centreId, 0L, cmd.name, cmd.description, Set.empty, Set.empty)
      } yield CentreEvent(newCentre.id.id).update(
        _.userId                    := cmd.userId,
        _.time                      := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.added.name                := cmd.name,
        _.added.optionalDescription := cmd.description
      )

    process (event) { applyAddedEvent(_) }
  }

  private def processUpdateCentreNameCmd(cmd: UpdateCentreNameCmd): Unit = {
    val v = updateDisabled(cmd) { centre =>
        for {
          nameAvailable <- nameAvailable(cmd.name, centre.id)
          centre        <- centre.withName(cmd.name)
        } yield CentreEvent(centre.id.id).update(
          _.userId              := cmd.userId,
          _.time                := ISODateTimeFormat.dateTime.print(DateTime.now),
          _.nameUpdated.version := cmd.expectedVersion,
          _.nameUpdated.name    := cmd.name
        )
      }

    process (v) { applyNameUpdatedEvent(_) }
  }

  private def processUpdateCentreDescriptionCmd(cmd: UpdateCentreDescriptionCmd): Unit = {
    val v = updateDisabled(cmd) { centre =>
        centre.withDescription(cmd.description).map { _ =>
          CentreEvent(centre.id.id).update(
            _.userId                                 := cmd.userId,
            _.time                                   := ISODateTimeFormat.dateTime.print(DateTime.now),
            _.descriptionUpdated.version             := cmd.expectedVersion,
            _.descriptionUpdated.optionalDescription := cmd.description
          )
        }
      }

    process (v) { applyDescriptionUpdatedEvent(_) }
  }

  private def processEnableCentreCmd(cmd: EnableCentreCmd): Unit = {
    val v = updateDisabled(cmd) { centre =>
        centre.enable.map { _ =>
          CentreEvent(centre.id.id).update(
            _.userId          := cmd.userId,
            _.time            := ISODateTimeFormat.dateTime.print(DateTime.now),
            _.enabled.version := cmd.expectedVersion)
        }
      }

    process (v) { applyEnabledEvent(_) }
  }

  private def processDisableCentreCmd(cmd: DisableCentreCmd): Unit = {
    val v = updateEnabled(cmd) { centre =>
        centre.disable.map { _ =>
          CentreEvent(centre.id.id).update(
            _.userId           := cmd.userId,
            _.time             := ISODateTimeFormat.dateTime.print(DateTime.now),
            _.disabled.version := cmd.expectedVersion)
        }
      }

    process (v) { applyDisabledEvent(_) }
  }

  /**
   * FIXME: Locations should be added regardless of the centre's status.
   */
  private def processAddLocationCmd(cmd: AddCentreLocationCmd): Unit = {
    val event = updateDisabled(cmd) { centre =>
        for {
          location <- {
            // need to call Location.create so that a new uniqueId is generated
            Location.create(cmd.name, cmd.street, cmd.city, cmd.province,
                            cmd.postalCode, cmd.poBoxNumber, cmd.countryIsoCode)
          }
          updatedCentre <- centre.withLocation(location)
        } yield CentreEvent(centre.id.id).update(
          _.userId                                     := cmd.userId,
          _.time                                       := ISODateTimeFormat.dateTime.print(DateTime.now),
          _.locationAdded.version                      := cmd.expectedVersion,
          _.locationAdded.location.locationId          := location.uniqueId,
          _.locationAdded.location.name                := cmd.name,
          _.locationAdded.location.street              := cmd.street,
          _.locationAdded.location.city                := cmd.city,
          _.locationAdded.location.province            := cmd.province,
          _.locationAdded.location.postalCode          := cmd.postalCode,
          _.locationAdded.location.optionalPoBoxNumber := cmd.poBoxNumber,
          _.locationAdded.location.countryIsoCode      := cmd.countryIsoCode)
      }

    process (event) { applyLocationAddedEvent(_) }
  }

  /**
   * FIXME: Locations should be added regardless of the centre's status.
   */
  private def processUpdateLocationCmd(cmd: UpdateCentreLocationCmd): Unit = {
    val event = updateDisabled(cmd) { centre =>
        for {
          location <- {
            // need to call Location.create so that a new uniqueId is generated
            Location(uniqueId       = cmd.locationId,
                     name           = cmd.name,
                     street         = cmd.street,
                     city           = cmd.city,
                     province       = cmd.province,
                     postalCode     = cmd.postalCode,
                     poBoxNumber    = cmd.poBoxNumber,
                     countryIsoCode = cmd.countryIsoCode).success
          }
          updatedCentre <- centre.withLocation(location)
        } yield CentreEvent(centre.id.id).update(
          _.userId                                       := cmd.userId,
          _.time                                         := ISODateTimeFormat.dateTime.print(DateTime.now),
          _.locationUpdated.version                      := cmd.expectedVersion,
          _.locationUpdated.location.locationId          := cmd.locationId,
          _.locationUpdated.location.name                := cmd.name,
          _.locationUpdated.location.street              := cmd.street,
          _.locationUpdated.location.city                := cmd.city,
          _.locationUpdated.location.province            := cmd.province,
          _.locationUpdated.location.postalCode          := cmd.postalCode,
          _.locationUpdated.location.optionalPoBoxNumber := cmd.poBoxNumber,
          _.locationUpdated.location.countryIsoCode      := cmd.countryIsoCode)
      }

    process (event) { applyLocationUpdatedEvent(_) }
  }

  /**
   * Locations can be removed regardless of the centre's status.
   */
  private def processRemoveLocationCmd(cmd: RemoveCentreLocationCmd): Unit = {
    val event = updateDisabled(cmd) { centre =>
        centre.removeLocation(cmd.locationId) map { _ =>
          CentreEvent(centre.id.id).update(
            _.userId                     := cmd.userId,
            _.time                       := ISODateTimeFormat.dateTime.print(DateTime.now),
            _.locationRemoved.version    := cmd.expectedVersion,
            _.locationRemoved.locationId := cmd.locationId)
        }
      }

    process (event) { applyLocationRemovedEvent(_) }
  }

  /**
   * Studies can be added regardless of the centre's status.
   */
  private def processAddStudyToCmd(cmd: AddStudyToCentreCmd): Unit = {
    val event = updateDisabled(cmd) { centre =>
        studyRepository.getByKey(StudyId(cmd.studyId)).map { _ =>
          CentreEvent(centre.id.id).update(
            _.userId             := cmd.userId,
            _.time               := ISODateTimeFormat.dateTime.print(DateTime.now),
            _.studyAdded.version := cmd.expectedVersion,
            _.studyAdded.studyId := cmd.studyId)
        }
      }

    process (event) { applyStudyAddedEvent(_) }
  }

  /**
   * Studies can be removed regardless of the centre's status.
   */
  private def processRemoveStudyCmd(cmd: RemoveStudyFromCentreCmd): Unit = {
    val event = updateDisabled(cmd) { centre =>
        for {
          studyExists <- studyRepository.getByKey(StudyId(cmd.studyId))
          canRemove   <- centre.removeStudyId(StudyId(cmd.studyId))
        } yield CentreEvent(centre.id.id).update(
          _.userId               := cmd.userId,
          _.time                 := ISODateTimeFormat.dateTime.print(DateTime.now),
          _.studyRemoved.version := cmd.expectedVersion,
          _.studyRemoved.studyId := cmd.studyId)
      }

    process (event) { applyRemovedFromStudyEvent(_) }
  }

  def onValidEventCentreAndVersion(event: CentreEvent,
                                   eventType: Boolean,
                                   eventVersion: Long)
                                  (fn: Centre => Unit): Unit = {
    if (!eventType) {
      log.error(s"invalid event type: $event")
    } else {
      centreRepository.getByKey(CentreId(event.id)).fold(
        err => log.error(s"centre from event does not exist: $err"),
        centre => {
          if (centre.version != eventVersion) {
            log.error(s"event version check failed: centre version: ${centre.version}, event: $event")
          } else {
            fn(centre)
          }
        }
      )
    }
  }

  def onValidEventDisabledCentreAndVersion(event: CentreEvent,
                                           eventType: Boolean,
                                           eventVersion: Long)
                                          (fn: DisabledCentre => Unit): Unit = {
    onValidEventCentreAndVersion(event, eventType, eventVersion) {
      case centre: DisabledCentre => fn(centre)
      case centre => log.error(s"$centre for $event is not disabled")
    }
  }

  def onValidEventEnabledCentreAndVersion(event: CentreEvent,
                                          eventType: Boolean,
                                          eventVersion: Long)
                                         (fn: EnabledCentre => Unit): Unit = {
    onValidEventCentreAndVersion(event, eventType, eventVersion) {
      case centre: EnabledCentre => fn(centre)
      case centre => log.error(s"$centre for $event is not enabled")
    }
  }

  private def applyAddedEvent(event: CentreEvent): Unit = {
    if (!event.eventType.isAdded) {
      log.error(s"invalid event type: $event")
    } else {
      val addedEvent = event.getAdded
      DisabledCentre.create(id           = CentreId(event.id),
                            version      = 0L,
                            name         = addedEvent.getName,
                            description  = addedEvent.description,
                            studyIds     = Set.empty,
                            locations    = Set.empty
      ).fold (
        err => log.error(s"could not add study from event: $event"),
        c => {
          centreRepository.put(
            c.copy(timeAdded = ISODateTimeFormat.dateTime.parseDateTime(event.getTime)))
          ()
        }
      )
    }
  }

  private def applyNameUpdatedEvent(event: CentreEvent): Unit = {
    onValidEventDisabledCentreAndVersion(event,
                                         event.eventType.isNameUpdated,
                                         event.getNameUpdated.getVersion) { centre =>
      val updatedEvent = event.getNameUpdated

      centre.withName(updatedEvent.getName).fold (
        err => log.error(s"updating centre from event failed: $err"),
        c => {
          centreRepository.put(
            c.copy(timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime))))
          ()
        }
      )
    }
  }

  private def applyDescriptionUpdatedEvent(event: CentreEvent): Unit = {
    onValidEventDisabledCentreAndVersion(event,
                                         event.eventType.isDescriptionUpdated,
                                         event.getDescriptionUpdated.getVersion) { centre =>
      val descriptionUpdated = event.getDescriptionUpdated

      centre.withDescription(descriptionUpdated.description).fold (
        err => log.error(s"updating centre from event failed: $err"),
        c => {
          centreRepository.put(
            c.copy(timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime))))
          ()
        }
      )
    }
  }

  private def applyEnabledEvent(event: CentreEvent): Unit = {
    onValidEventDisabledCentreAndVersion(event,
                                         event.eventType.isEnabled,
                                         event.getEnabled.getVersion) { centre =>
      val enabledEvent = event.getEnabled

      centre.enable.fold(
        err => log.error(s"enabling centre from event failed: $err"),
        c => {
          centreRepository.put(
            c.copy(timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime))))
          ()
        }
      )
    }
  }

  private def applyDisabledEvent(event: CentreEvent): Unit = {
    onValidEventEnabledCentreAndVersion(event,
                                        event.eventType.isDisabled,
                                        event.getDisabled.getVersion) { centre =>
      centre.disable.fold(
        err => log.error(s"disabling centre from event failed: $err"),
        c => {
          val disabledEvent = event.getDisabled
          centreRepository.put(
            c.copy(timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime))))
          ()
        }
      )
    }
  }

  private def applyLocationAddedEvent(event: CentreEvent): Unit = {
    onValidEventDisabledCentreAndVersion(event,
                                         event.eventType.isLocationAdded,
                                         event.getLocationAdded.getVersion) { centre =>
      val locationAddedEvent = event.getLocationAdded

      centre.withLocation(Location(uniqueId       = locationAddedEvent.getLocation.getLocationId,
                                   name           = locationAddedEvent.getLocation.getName,
                                   street         = locationAddedEvent.getLocation.getStreet,
                                   city           = locationAddedEvent.getLocation.getCity,
                                   province       = locationAddedEvent.getLocation.getProvince,
                                   postalCode     = locationAddedEvent.getLocation.getPostalCode,
                                   poBoxNumber    = locationAddedEvent.getLocation.poBoxNumber,
                                   countryIsoCode = locationAddedEvent.getLocation.getCountryIsoCode)
      ).fold(
        err => log.error(s"adding location from event failed: $err"),
        c => {
          centreRepository.put(
            c.copy(timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime))))
          ()
        }
      )
    }
  }

  private def applyLocationUpdatedEvent(event: CentreEvent): Unit = {
    onValidEventDisabledCentreAndVersion(event,
                                         event.eventType.isLocationUpdated,
                                         event.getLocationUpdated.getVersion) { centre =>
      val locationUpdatedEvent = event.getLocationUpdated

      centre.withLocation(Location(uniqueId       = locationUpdatedEvent.getLocation.getLocationId,
                                   name           = locationUpdatedEvent.getLocation.getName,
                                   street         = locationUpdatedEvent.getLocation.getStreet,
                                   city           = locationUpdatedEvent.getLocation.getCity,
                                   province       = locationUpdatedEvent.getLocation.getProvince,
                                   postalCode     = locationUpdatedEvent.getLocation.getPostalCode,
                                   poBoxNumber    = locationUpdatedEvent.getLocation.poBoxNumber,
                                   countryIsoCode = locationUpdatedEvent.getLocation.getCountryIsoCode)
      ).fold(
        err => log.error(s"adding location from event failed: $err"),
        c => {
          centreRepository.put(
            c.copy(timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime))))
          ()
        }
      )
    }
  }

  private def applyLocationRemovedEvent(event: CentreEvent): Unit = {
    onValidEventDisabledCentreAndVersion(event,
                                         event.eventType.isLocationRemoved,
                                         event.getLocationRemoved.getVersion) { centre =>

      val locationRemovedEvent = event.getLocationRemoved

      centre.removeLocation(locationRemovedEvent.getLocationId).fold(
        err => log.error(s"updating centre from event failed: $err"),
        c => {
          centreRepository.put(
            c.copy(timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime))))
          ()
        }
      )
    }
  }

  private def applyStudyAddedEvent(event: CentreEvent): Unit = {
    onValidEventDisabledCentreAndVersion(event,
                                         event.eventType.isStudyAdded,
                                         event.getStudyAdded.getVersion) { centre =>
      val studyAddedEvent = event.getStudyAdded

      centre.withStudyId(StudyId(studyAddedEvent.getStudyId)).fold(
        err => log.error(s"updating centre from event failed: $err"),
        c => {
          centreRepository.put(
            c.copy(timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime))))
          ()
        }
      )
    }
  }

  private def applyRemovedFromStudyEvent(event: CentreEvent): Unit = {
    onValidEventDisabledCentreAndVersion(event,
                                         event.eventType.isStudyRemoved,
                                         event.getStudyRemoved.getVersion) { centre =>
      val studyRemovedEvent = event.getStudyRemoved

      centre.removeStudyId(StudyId(studyRemovedEvent.getStudyId)).fold(
        err => log.error(s"updating centre from event failed: $err"),
        c => {
          centreRepository.put(
            c.copy(timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime))))
          ()
        }
      )
    }
  }

  private def nameAvailable(name: String): DomainValidation[Boolean] = {
    nameAvailableMatcher(name, centreRepository, ErrMsgNameExists){ item =>
      item.name == name
    }
  }

  private def nameAvailable(name: String, excludeId: CentreId): DomainValidation[Boolean] = {
    nameAvailableMatcher(name, centreRepository, ErrMsgNameExists){ item =>
      (item.name == name) && (item.id != excludeId)
    }
  }

  private def updateCentre[T <: Centre](cmd: CentreModifyCommand)
                          (fn: Centre => DomainValidation[CentreEvent])
      : DomainValidation[CentreEvent] = {
    for {
      centre <- centreRepository.getByKey(CentreId(cmd.id))
      validVersion  <-  centre.requireVersion(cmd.expectedVersion)
      updatedCentre <- fn(centre)
    } yield updatedCentre
  }

  private def updateDisabled[T <: Centre](cmd: CentreModifyCommand)
                            (fn: DisabledCentre => DomainValidation[CentreEvent])
      : DomainValidation[CentreEvent] = {
    updateCentre(cmd) {
      case centre: DisabledCentre => fn(centre)
      case centre => InvalidStatus(s"centre is not disabled: ${cmd.id}").failureNel
    }
  }

  private def updateEnabled[T <: Centre](cmd: CentreModifyCommand)
                           (fn: EnabledCentre => DomainValidation[CentreEvent])
      : DomainValidation[CentreEvent] = {
    updateCentre(cmd) {
      case centre: EnabledCentre => fn(centre)
      case centre => InvalidStatus(s"centre is not enabled: ${cmd.id}").failureNel
    }
  }

  testData.addMultipleCentres
}
