package org.biobank.service.centres

import akka.actor._
import akka.persistence.{ RecoveryCompleted, SnapshotOffer }
import javax.inject.{Inject}
import org.biobank.TestData
import org.biobank.domain.centre._
import org.biobank.domain.study.{StudyId, StudyRepository}
import org.biobank.domain.{DomainValidation, DomainError, Location}
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

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
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
      case et: EventType.StudyRemoved       => applyStudyRemovedEvent(event)

      case et => log.error(s"event not handled: $event")
    }

    case SnapshotOffer(_, snapshot: SnapshotState) =>
      snapshot.centres.foreach{ centre => centreRepository.put(centre) }

    case event: RecoveryCompleted => // silence these messages

    case cmd => log.error(s"CentresProcessor: message not handled: $cmd")
  }

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  val receiveCommand: Receive = {
    case cmd: AddCentreCmd =>
      process(addCentreCmdToEvent(cmd))(applyAddedEvent)

    case cmd: UpdateCentreNameCmd =>
      processUpdateCmdOnDisabledCentre(cmd, updateCentreNameCmdToEvent, applyNameUpdatedEvent)

    case cmd: UpdateCentreDescriptionCmd =>
      processUpdateCmdOnDisabledCentre(cmd, updateCentreDescriptionCmdToEvent, applyDescriptionUpdatedEvent)

    case cmd: EnableCentreCmd =>
      processUpdateCmdOnDisabledCentre(cmd, enableCentreCmdToEvent, applyEnabledEvent)

    case cmd: DisableCentreCmd =>
      processUpdateCmdOnEnabledCentre(cmd, disableCentreCmdToEvent, applyDisabledEvent)

    case cmd: AddCentreLocationCmd =>
      processUpdateCmdOnDisabledCentre(cmd, addLocationCmdToEvent, applyLocationAddedEvent)

    case cmd: UpdateCentreLocationCmd =>
      processUpdateCmdOnDisabledCentre(cmd, updateLocationCmdToEvent, applyLocationUpdatedEvent)

    case cmd: RemoveCentreLocationCmd =>
      processUpdateCmdOnDisabledCentre(cmd, removeLocationCmdToEvent, applyLocationRemovedEvent)

    case cmd: AddStudyToCentreCmd =>
      processUpdateCmdOnDisabledCentre(cmd, addStudyCmdToEvent, applyStudyAddedEvent)

    case cmd: RemoveStudyFromCentreCmd =>
      processUpdateCmdOnDisabledCentre(cmd, removeStudyCmdToEvent, applyStudyRemovedEvent)

    case "snap" =>
      saveSnapshot(SnapshotState(centreRepository.getValues.toSet))
      stash()

    case cmd => log.error(s"CentresProcessor: message not handled: $cmd")
  }

  private def addCentreCmdToEvent(cmd: AddCentreCmd): DomainValidation[CentreEvent] = {
    for {
      centreId      <- validNewIdentity(centreRepository.nextIdentity, centreRepository)
      nameAvailable <- nameAvailable(cmd.name)
      newCentre     <- DisabledCentre.create(id          = centreId,
                                             version     = 0L,
                                             name        = cmd.name,
                                             description = cmd.description,
                                             studyIds    = Set.empty,
                                             locations   = Set.empty)
    } yield CentreEvent(newCentre.id.id).update(
      _.userId                    := cmd.userId,
      _.time                      := ISODateTimeFormat.dateTime.print(DateTime.now),
      _.added.name                := cmd.name,
      _.added.optionalDescription := cmd.description
    )
  }

  private def updateCentreNameCmdToEvent(cmd:    UpdateCentreNameCmd,
                                         centre: DisabledCentre): DomainValidation[CentreEvent] = {
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

  private def updateCentreDescriptionCmdToEvent(cmd:    UpdateCentreDescriptionCmd,
                                                centre: DisabledCentre): DomainValidation[CentreEvent] = {
    centre.withDescription(cmd.description).map { _ =>
      CentreEvent(centre.id.id).update(
        _.userId                                 := cmd.userId,
        _.time                                   := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.descriptionUpdated.version             := cmd.expectedVersion,
        _.descriptionUpdated.optionalDescription := cmd.description
      )
    }
  }

  private def enableCentreCmdToEvent(cmd:    EnableCentreCmd,
                                     centre: DisabledCentre): DomainValidation[CentreEvent] = {
    centre.enable.map { _ =>
      CentreEvent(centre.id.id).update(
        _.userId          := cmd.userId,
        _.time            := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.enabled.version := cmd.expectedVersion)
    }
  }

  private def disableCentreCmdToEvent(cmd:    DisableCentreCmd,
                                      centre: EnabledCentre): DomainValidation[CentreEvent] = {
    centre.disable.map { _ =>
      CentreEvent(centre.id.id).update(
        _.userId           := cmd.userId,
        _.time             := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.disabled.version := cmd.expectedVersion)
    }
  }

  /**
   * FIXME: Locations should be added regardless of the centre's status.
   */
  private def addLocationCmdToEvent(cmd:    AddCentreLocationCmd,
                                    centre: DisabledCentre): DomainValidation[CentreEvent] = {
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

  /**
   * FIXME: Locations should be added regardless of the centre's status.
   */
  private def updateLocationCmdToEvent(cmd:    UpdateCentreLocationCmd,
                                       centre: DisabledCentre): DomainValidation[CentreEvent] = {
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
                 countryIsoCode = cmd.countryIsoCode).successNel[String]
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

  /**
   * Locations can be removed regardless of the centre's status.
   */
  private def removeLocationCmdToEvent(cmd:    RemoveCentreLocationCmd,
                                       centre: DisabledCentre): DomainValidation[CentreEvent] = {
    centre.removeLocation(cmd.locationId) map { _ =>
      CentreEvent(centre.id.id).update(
        _.userId                     := cmd.userId,
        _.time                       := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.locationRemoved.version    := cmd.expectedVersion,
        _.locationRemoved.locationId := cmd.locationId)
    }
  }

  /**
   * Studies can be added regardless of the centre's status.
   */
  private def addStudyCmdToEvent(cmd: AddStudyToCentreCmd,
                                 centre: DisabledCentre): DomainValidation[CentreEvent] = {
    studyRepository.getByKey(StudyId(cmd.studyId)).map { _ =>
      CentreEvent(centre.id.id).update(
        _.userId             := cmd.userId,
        _.time               := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.studyAdded.version := cmd.expectedVersion,
        _.studyAdded.studyId := cmd.studyId)
    }
  }

  /**
   * Studies can be removed regardless of the centre's status.
   */
  private def removeStudyCmdToEvent(cmd:    RemoveStudyFromCentreCmd,
                                    centre: DisabledCentre): DomainValidation[CentreEvent] = {
    for {
      studyExists <- studyRepository.getByKey(StudyId(cmd.studyId))
      canRemove   <- centre.removeStudyId(StudyId(cmd.studyId))
    } yield CentreEvent(centre.id.id).update(
      _.userId               := cmd.userId,
      _.time                 := ISODateTimeFormat.dateTime.print(DateTime.now),
      _.studyRemoved.version := cmd.expectedVersion,
      _.studyRemoved.studyId := cmd.studyId)
  }

  private def onValidEventCentreAndVersion(event:        CentreEvent,
                                           eventType:    Boolean,
                                           eventVersion: Long)
                                          (applyEvent: (Centre,
                                                        CentreEvent,
                                                        DateTime) => DomainValidation[Centre])
      : Unit = {
    if (!eventType) {
      log.error(s"invalid event type: $event")
    } else {
      centreRepository.getByKey(CentreId(event.id)).fold(
        err => log.error(s"centre from event does not exist: $err"),
        centre => {
          if (centre.version != eventVersion) {
            log.error(s"event version check failed: centre version: ${centre.version}, event: $event")
          } else {
            val eventTime = ISODateTimeFormat.dateTime.parseDateTime(event.getTime)
            val update = applyEvent(centre, event, eventTime)

            if (update.isFailure) {
              log.error(s"centre update from event failed: $update")
            }
          }
        }
      )
    }
  }

  private def onValidEventDisabledCentreAndVersion(event:        CentreEvent,
                                                   eventType:    Boolean,
                                                   eventVersion: Long)
                                                  (applyEvent: (DisabledCentre,
                                                                CentreEvent,
                                                                DateTime) => DomainValidation[Centre])
      : Unit = {
    onValidEventCentreAndVersion(event, eventType, eventVersion) { (centre, event, eventTime) =>
      centre match {
        case c: DisabledCentre => applyEvent(c, event, eventTime)
        case c => DomainError(s"centre is not disabled: $centre").failureNel[DisabledCentre]
      }
    }
  }

  private def onValidEventEnabledCentreAndVersion(event:        CentreEvent,
                                                  eventType:    Boolean,
                                                  eventVersion: Long)
                                                 (applyEvent: (EnabledCentre,
                                                               CentreEvent,
                                                               DateTime) => DomainValidation[Centre])
      : Unit = {
    onValidEventCentreAndVersion(event, eventType, eventVersion) { (centre, event, eventTime) =>
      centre match {
        case c: EnabledCentre => applyEvent(c, event, eventTime)
        case c => DomainError(s"centre is not enabled: $centre").failureNel[EnabledCentre]
      }
    }
  }

  private def applyAddedEvent(event: CentreEvent): Unit = {
    if (!event.eventType.isAdded) {
      log.error(s"invalid event type: $event")
    } else {
      val addedEvent = event.getAdded
      val validation = DisabledCentre.create(id           = CentreId(event.id),
                                             version      = 0L,
                                             name         = addedEvent.getName,
                                             description  = addedEvent.description,
                                             studyIds     = Set.empty,
                                             locations    = Set.empty)

      if (validation.isFailure) {
        log.error(s"could not add study from event: $event")
      }

      validation.foreach { c =>
        centreRepository.put(
          c.copy(timeAdded = ISODateTimeFormat.dateTime.parseDateTime(event.getTime)))
      }
    }
  }

  private def applyNameUpdatedEvent(event: CentreEvent): Unit = {
    onValidEventDisabledCentreAndVersion(event,
                                         event.eventType.isNameUpdated,
                                         event.getNameUpdated.getVersion) { (centre, _, eventTime) =>
      val v = centre.withName(event.getNameUpdated.getName)
      v.foreach { c => centreRepository.put(c.copy(timeModified = Some(eventTime))) }
      v
    }
  }

  private def applyDescriptionUpdatedEvent(event: CentreEvent): Unit = {
    onValidEventDisabledCentreAndVersion(event,
                                         event.eventType.isDescriptionUpdated,
                                         event.getDescriptionUpdated.getVersion) { (centre, _, eventTime) =>
      val v = centre.withDescription(event.getDescriptionUpdated.description)
      v.foreach { c => centreRepository.put(c.copy(timeModified = Some(eventTime))) }
      v
    }
  }

  private def applyEnabledEvent(event: CentreEvent): Unit = {
    onValidEventDisabledCentreAndVersion(event,
                                         event.eventType.isEnabled,
                                         event.getEnabled.getVersion) { (centre, _, eventTime) =>
      val v = centre.enable
      v.foreach { c => centreRepository.put(c.copy(timeModified = Some(eventTime))) }
      v
    }
  }

  private def applyDisabledEvent(event: CentreEvent): Unit = {
    onValidEventEnabledCentreAndVersion(event,
                                        event.eventType.isDisabled,
                                        event.getDisabled.getVersion) { (centre, _, eventTime) =>
      val v = centre.disable
      v.foreach { c => centreRepository.put(c.copy(timeModified = Some(eventTime))) }
      v
    }
  }

  private def applyLocationAddedEvent(event: CentreEvent): Unit = {
    onValidEventDisabledCentreAndVersion(event,
                                         event.eventType.isLocationAdded,
                                         event.getLocationAdded.getVersion) { (centre, _, eventTime) =>
      val locationAddedEvent = event.getLocationAdded

      val v = centre.withLocation(Location(uniqueId       = locationAddedEvent.getLocation.getLocationId,
                                           name           = locationAddedEvent.getLocation.getName,
                                           street         = locationAddedEvent.getLocation.getStreet,
                                           city           = locationAddedEvent.getLocation.getCity,
                                           province       = locationAddedEvent.getLocation.getProvince,
                                           postalCode     = locationAddedEvent.getLocation.getPostalCode,
                                           poBoxNumber    = locationAddedEvent.getLocation.poBoxNumber,
                                           countryIsoCode = locationAddedEvent.getLocation.getCountryIsoCode))
      v.foreach { c => centreRepository.put(c.copy(timeModified = Some(eventTime))) }
      v
    }
  }

  private def applyLocationUpdatedEvent(event: CentreEvent): Unit = {
    onValidEventDisabledCentreAndVersion(event,
                                         event.eventType.isLocationUpdated,
                                         event.getLocationUpdated.getVersion) { (centre, _, eventTime) =>
      val locationUpdatedEvent = event.getLocationUpdated
      val v = centre.withLocation(Location(uniqueId       = locationUpdatedEvent.getLocation.getLocationId,
                                           name           = locationUpdatedEvent.getLocation.getName,
                                           street         = locationUpdatedEvent.getLocation.getStreet,
                                           city           = locationUpdatedEvent.getLocation.getCity,
                                           province       = locationUpdatedEvent.getLocation.getProvince,
                                           postalCode     = locationUpdatedEvent.getLocation.getPostalCode,
                                           poBoxNumber    = locationUpdatedEvent.getLocation.poBoxNumber,
                                           countryIsoCode = locationUpdatedEvent.getLocation.getCountryIsoCode))
      v.foreach { c => centreRepository.put(c.copy(timeModified = Some(eventTime))) }
      v
    }
  }

  private def applyLocationRemovedEvent(event: CentreEvent): Unit = {
    onValidEventDisabledCentreAndVersion(event,
                                         event.eventType.isLocationRemoved,
                                         event.getLocationRemoved.getVersion) { (centre, _, eventTime) =>
      val v = centre.removeLocation(event.getLocationRemoved.getLocationId)
      v.foreach { c => centreRepository.put(c.copy(timeModified = Some(eventTime))) }
      v
    }
  }

  private def applyStudyAddedEvent(event: CentreEvent): Unit = {
    onValidEventDisabledCentreAndVersion(event,
                                         event.eventType.isStudyAdded,
                                         event.getStudyAdded.getVersion) { (centre, _, eventTime) =>
      val v = centre.withStudyId(StudyId(event.getStudyAdded.getStudyId))
      v.foreach { c => centreRepository.put(c.copy(timeModified = Some(eventTime))) }
      v
    }
  }

  private def applyStudyRemovedEvent(event: CentreEvent): Unit = {
    onValidEventDisabledCentreAndVersion(event,
                                         event.eventType.isStudyRemoved,
                                         event.getStudyRemoved.getVersion) { (centre, _, eventTime) =>
      val v = centre.removeStudyId(StudyId(event.getStudyRemoved.getStudyId))
      v.foreach { c => centreRepository.put(c.copy(timeModified = Some(eventTime))) }
      v
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  private def nameAvailable(name: String): DomainValidation[Boolean] = {
    nameAvailableMatcher(name, centreRepository, ErrMsgNameExists){ item =>
      item.name == name
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  private def nameAvailable(name: String, excludeId: CentreId): DomainValidation[Boolean] = {
    nameAvailableMatcher(name, centreRepository, ErrMsgNameExists){ item =>
      (item.name == name) && (item.id != excludeId)
    }
  }

  private def processUpdateCmd[T <: CentreModifyCommand]
    (cmd:            T,
     commandToEvent: (T, Centre) => DomainValidation[CentreEvent],
     applyEvent:     CentreEvent => Unit): Unit = {
    val event = for {
        centre       <- centreRepository.getByKey(CentreId(cmd.id))
        validVersion <-  centre.requireVersion(cmd.expectedVersion)
        event        <- commandToEvent(cmd, centre)
      } yield event

    process(event)(applyEvent)
  }

  private def processUpdateCmdOnDisabledCentre[T <: CentreModifyCommand]
    (cmd:            T,
     commandToEvent: (T, DisabledCentre) => DomainValidation[CentreEvent],
     applyEvent:     CentreEvent => Unit): Unit = {

    def internal(cmd: T, centre: Centre): DomainValidation[CentreEvent] = {
      centre match {
        case c: DisabledCentre => commandToEvent(cmd, c)
        case c => InvalidStatus(s"centre is not disabled: ${cmd.id}").failureNel[CentreEvent]
      }
    }

    processUpdateCmd(cmd, internal, applyEvent)
  }

  private def processUpdateCmdOnEnabledCentre[T <: CentreModifyCommand]
    (cmd:            T,
     commandToEvent: (T, EnabledCentre) => DomainValidation[CentreEvent],
     applyEvent:     CentreEvent => Unit): Unit = {

    def internal(cmd: T, centre: Centre): DomainValidation[CentreEvent] = {
      centre match {
        case c: EnabledCentre => commandToEvent(cmd, c)
        case c => InvalidStatus(s"centre is not enabled: ${cmd.id}").failureNel[CentreEvent]
      }
    }

    processUpdateCmd(cmd, internal, applyEvent)
  }

  testData.addMultipleCentres
}
