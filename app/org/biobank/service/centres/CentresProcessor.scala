package org.biobank.service.centres

import org.biobank.service.Processor
import org.biobank.domain.centre._
import org.biobank.domain.study.StudyId
import org.biobank.domain.{
  DomainValidation,
  DomainError,
  Location,
  LocationId,
  LocationRepository
}
import org.biobank.domain.user.UserId
import org.biobank.infrastructure.command.CentreCommands._
import org.biobank.infrastructure.event.CentreEvents._
import org.biobank.TestData

import akka.actor._
import akka.pattern.ask
import akka.persistence.{ RecoveryCompleted, SnapshotOffer }
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.slf4j.LoggerFactory
import javax.inject.{Inject => javaxInject}

import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

object CentresProcessor {

  def props = Props[CentresProcessor]

}

class CentresProcessor @javaxInject() (val centreRepository:          CentreRepository,
                                       val locationRepository:        LocationRepository,
                                       val centreStudiesRepository:   CentreStudiesRepository,
                                       val centreLocationsRepository: CentreLocationsRepository,
                                       val testData:                  TestData)
    extends Processor {
  import CentreEvent.EventType

  override def persistenceId = "centre-processor-id"

  case class SnapshotState(centres: Set[Centre])

  val errMsgNameExists = "centre with name already exists"

  val receiveRecover: Receive = {
    case event: CentreEvent => event.eventType match {
        case et: EventType.Added           => applyCentreAddedEvent(event)
        case et: EventType.Updated         => applyCentreUpdatedEvent(event)
        case et: EventType.Enabled         => applyCentreEnabledEvent(event)
        case et: EventType.Disabled        => applyCentreDisabledEvent(event)
        case et: EventType.LocationAdded   => applyCentreLocationAddedEvent(event)
        case et: EventType.LocationRemoved => applyCentreLocationRemovedEvent(event)
        case et: EventType.StudyAdded      => applyCentreAddedToStudyEvent(event)
        case et: EventType.StudyRemoved    => applyCentreRemovedFromStudyEvent(event)

        case et => log.error(s"event not handled: $event")
      }

    case SnapshotOffer(_, snapshot: SnapshotState) =>
      snapshot.centres.foreach{ centre => centreRepository.put(centre) }

    case event: RecoveryCompleted => // silence these messages

    case cmd => log.error(s"CentresProcessor: message not handled: $cmd")
  }

  val receiveCommand: Receive = {
    case cmd: AddCentreCmd             => processAddCentreCmd(cmd)
    case cmd: UpdateCentreCmd          => processUpdateCentreCmd(cmd)
    case cmd: EnableCentreCmd          => processEnableCentreCmd(cmd)
    case cmd: DisableCentreCmd         => processDisableCentreCmd(cmd)
    case cmd: AddCentreLocationCmd     => processAddCentreLocationCmd(cmd)
    case cmd: RemoveCentreLocationCmd  => processRemoveCentreLocationCmd(cmd)
    case cmd: AddStudyToCentreCmd      => processAddStudyToCentreCmd(cmd)
    case cmd: RemoveStudyFromCentreCmd => processRemoveStudyFromCentreCmd(cmd)

    case "snap" =>
      saveSnapshot(SnapshotState(centreRepository.getValues.toSet))
      stash()

    case cmd => log.error(s"CentresProcessor: message not handled: $cmd")
  }

  private def processAddCentreCmd(cmd: AddCentreCmd): Unit = {
    val timeNow = DateTime.now
    val centreId = centreRepository.nextIdentity

    if (centreRepository.getByKey(centreId).isSuccess) {
      log.error(s"centre with id already exsits: $centreId")
    }

    val event = for {
      nameAvailable <- nameAvailable(cmd.name)
      newCentre <- DisabledCentre.create(centreId, -1L, timeNow, cmd.name, cmd.description)
      event <- createCentreEvent(newCentre.id, cmd).withAdded(
        CentreAddedEvent(name = Some(newCentre.name),
                         description = newCentre.description)).success
    } yield event

    process (event) { applyCentreAddedEvent(_) }
  }

  private def processUpdateCentreCmd(cmd: UpdateCentreCmd): Unit = {
    val v = updateDisabled(cmd) { centre =>
      for {
        nameAvailable <- nameAvailable(cmd.name, centre.id)
        updatedCentre <- centre.update(cmd.name, cmd.description)
      } yield updatedCentre
    }

    val event = v.fold(
      err => err.failure,
      centre => createCentreEvent(centre.id, cmd).withUpdated(
        CentreUpdatedEvent(version = Some(centre.version),
                           name = Some(centre.name),
                           description = centre.description)).success
    )

    process (event) { applyCentreUpdatedEvent(_) }
  }

  private def processEnableCentreCmd(cmd: EnableCentreCmd): Unit = {
    val timeNow = DateTime.now
    val v = updateDisabled(cmd) { c => c.enable }

    val  event = v.fold(
      err => err.failure,
      centre => createCentreEvent(centre.id, cmd).withEnabled(
          CentreEnabledEvent(version = Some(centre.version))).success
    )

    process (event) { applyCentreEnabledEvent(_) }
  }

  private def processDisableCentreCmd(cmd: DisableCentreCmd): Unit = {
    val v = updateEnabled(cmd) { c => c.disable }
    val event = v.fold(
      err => err.failure,
      centre => createCentreEvent(centre.id, cmd).withDisabled(
        CentreDisabledEvent(version = Some(centre.version))).success
    )

    process (event) { applyCentreDisabledEvent(_) }
  }

  /**
   * Locations can be added regardless of the centre's status.
   */
  private def processAddCentreLocationCmd(cmd: AddCentreLocationCmd): Unit = {
    val locationId = locationRepository.nextIdentity

    val event = centreRepository.getByKey(CentreId(cmd.centreId)).fold(
      err => DomainError(s"centre with id does not exist: $id").failureNel,
      centre => {
        for {
          location <- Location(locationId, cmd.name, cmd.street, cmd.city, cmd.province,
                               cmd.postalCode, cmd.poBoxNumber, cmd.countryIsoCode).success
          event <- createCentreEvent(centre.id, cmd).withLocationAdded(
            CentreLocationAddedEvent(locationId     = Some(locationId.id),
                                     name           = Some(cmd.name),
                                     street         = Some(cmd.street),
                                     city           = Some(cmd.city),
                                     province       = Some(cmd.province),
                                     postalCode     = Some(cmd.postalCode),
                                     poBoxNumber    = cmd.poBoxNumber,
                                     countryIsoCode = Some(cmd.countryIsoCode))).success
        } yield event
      }
    )

    process (event) { applyCentreLocationAddedEvent(_) }
  }

  /**
   * Locations can be removed regardless of the centre's status.
   */
  private def processRemoveCentreLocationCmd(cmd: RemoveCentreLocationCmd): Unit = {
    val event = centreRepository.getByKey(CentreId(cmd.centreId)).fold(
      err => DomainError(s"centre with id does not exist: $id").failureNel,
      centre => {
        locationRepository.getByKey(LocationId(cmd.locationId)).fold(
          err => DomainError(s"location with id does not exist: $id").failureNel,
          location => createCentreEvent(centre.id, cmd).withLocationRemoved(
            CentreLocationRemovedEvent(Some(cmd.locationId))).success
        )
      }
    )

    process (event) { applyCentreLocationRemovedEvent(_) }
  }

  /**
   * Studies can be added regardless of the centre's status.
   */
  private def processAddStudyToCentreCmd(cmd: AddStudyToCentreCmd): Unit = {
    val event = centreRepository.getByKey(CentreId(cmd.centreId)).fold(
      err => DomainError(s"centre with id does not exist: $id").failureNel,
      centre => {
        for {
          notLinked <- centreStudyNotLinked(centre.id, StudyId(cmd.studyId))
          event <- createCentreEvent(centre.id, cmd).withStudyAdded(
            StudyAddedToCentreEvent(Some(cmd.studyId))).success
        } yield event
      }
    )

    process (event) { applyCentreAddedToStudyEvent(_) }
  }

  /**
   * Studies can be removed regardless of the centre's status.
   */
  private def processRemoveStudyFromCentreCmd(cmd: RemoveStudyFromCentreCmd): Unit = {
    val event = for {
      centre <- centreRepository.getByKey(CentreId(cmd.centreId))
      item <- centreStudiesRepository.withIds(StudyId(cmd.studyId), centre.id)
      event <- createCentreEvent(centre.id, cmd).withStudyRemoved(
        StudyRemovedFromCentreEvent(Some(cmd.studyId))).success
    } yield event

    process (event) { applyCentreRemovedFromStudyEvent(_) }
  }

  private def applyCentreAddedEvent(event: CentreEvent): Unit = {
    if (event.eventType.isAdded) {
      val addedEvent = event.getAdded

      centreRepository.put(
        DisabledCentre(id           = CentreId(event.id),
                       version      = 0L,
                       timeAdded    = ISODateTimeFormat.dateTime.parseDateTime(event.getTime),
                       timeModified = None,
                       name         = addedEvent.getName,
                       description  = addedEvent.description))
      ()
    } else {
      log.error(s"invalid event type: $event")
    }
  }

  private def applyCentreUpdatedEvent(event: CentreEvent): Unit = {
    if (event.eventType.isUpdated) {
      val updatedEvent = event.getUpdated

      getDisabled(event.id).fold(
        err => log.error(s"updating centre from event failed: $err"),
        centre => {
          centreRepository.put(
            centre.copy(version      = updatedEvent.getVersion,
                        timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime)),
                        name         = updatedEvent.getName,
                        description  = updatedEvent.description))
          ()
        }
      )
    } else {
      log.error(s"invalid event type: $event")
    }
  }

  private def applyCentreEnabledEvent(event: CentreEvent): Unit = {
    if (event.eventType.isEnabled) {
      val enabledEvent = event.getEnabled

      getDisabled(event.id).fold(
        err => log.error(s"enabling centre from event failed: $err"),
        centre => {
          centreRepository.put(
            EnabledCentre(id           = CentreId(event.id),
                          version      = event.getEnabled.getVersion,
                          timeAdded    = centre.timeAdded,
                          timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime)),
                          name         = centre.name,
                          description  = centre.description))
          ()
        }
      )
    } else {
      log.error(s"invalid event type: $event")
    }
  }

  private def applyCentreDisabledEvent(event: CentreEvent): Unit = {
    if (event.eventType.isDisabled) {
      val disabledEvent = event.getDisabled

      getEnabled(event.id).fold(
        err => log.error(s"disabling centre from event failed: $err"),
        centre => {
          centreRepository.put(
            DisabledCentre(id           = CentreId(event.id),
                           version      = event.getDisabled.getVersion,
                           timeAdded    = centre.timeAdded,
                           timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime)),
                           name         = centre.name,
                           description  = centre.description))
          ()
        }
      )
    } else {
      log.error(s"invalid event type: $event")
    }
  }

  private def applyCentreLocationAddedEvent(event: CentreEvent): Unit = {
    if (event.eventType.isLocationAdded) {
      val locationAddedEvent = event.getLocationAdded

      val centreId = CentreId(event.id)
      val locationId = LocationId(locationAddedEvent.getLocationId)

      locationRepository.put(
        Location(id             = locationId,
                 name           = locationAddedEvent.getName,
                 street         = locationAddedEvent.getStreet,
                 city           = locationAddedEvent.getCity,
                 province       = locationAddedEvent.getProvince,
                 postalCode     = locationAddedEvent.getPostalCode,
                 poBoxNumber    = locationAddedEvent.poBoxNumber,
                 countryIsoCode = locationAddedEvent.getCountryIsoCode))
      centreLocationsRepository.put(CentreLocation(centreId, locationId))
      ()
    } else {
      log.error(s"invalid event type: $event")
    }
  }

  private def applyCentreLocationRemovedEvent(event: CentreEvent): Unit = {
    if (event.eventType.isLocationRemoved) {
      val locationRemovedEvent = event.getLocationRemoved

      val centreId = CentreId(event.id)
      val locationId = LocationId(locationRemovedEvent.getLocationId)

      val validation = for {
        location <- locationRepository.getByKey(locationId)
        removed <- locationRepository.remove(location).success
        link <- centreLocationsRepository.remove(CentreLocation(centreId, locationId)).success
      } yield location

      if (validation.isFailure) {
        // this should never happen because the only way to get here is when the
        // command passed validation
        log.error("removing centre location with event failed")
      }
    } else {
      log.error(s"invalid event type: $event")
    }
  }

  private def applyCentreAddedToStudyEvent(event: CentreEvent): Unit = {
    if (event.eventType.isStudyAdded) {
      val studyAddedEvent = event.getStudyAdded

      val centreId = CentreId(event.id)
      val studyId = StudyId(studyAddedEvent.getStudyId)
      val studyCentreId = centreStudiesRepository.nextIdentity
      centreStudiesRepository.put(StudyCentre(studyCentreId, studyId, centreId))
      ()
    } else {
      log.error(s"invalid event type: $event")
    }
  }

  private def applyCentreRemovedFromStudyEvent(event: CentreEvent): Unit = {
    if (event.eventType.isStudyRemoved) {
      val studyRemovedEvent = event.getStudyRemoved

      val centreId = CentreId(event.id)
      val studyId = StudyId(studyRemovedEvent.getStudyId)

      val validation = for {
        studyCentre <- centreStudiesRepository.withIds(studyId, centreId)
        removed <- centreStudiesRepository.remove(studyCentre).success
      } yield removed

      if (validation.isFailure) {
        // this should never happen because the only way to get here is when the
        // command passed validation
        log.error(s"removing study centre link with event failed: $validation")
      }
    } else {
      log.error(s"invalid event type: $event")
    }
  }

  /** Returns true if the centre and study are NOT linked.
    *
    */
  private def centreStudyNotLinked(centreId: CentreId, studyId: StudyId): DomainValidation[Boolean] = {
    val exists = centreStudiesRepository.getValues.exists { x =>
      (x.centreId == centreId) && (x.studyId == studyId)
    }
    if (exists) {
      DomainError(s"centre and study already linked: { centreId: $centreId, studyId: $studyId }").failureNel
    } else {
      true.success
    }
  }

  private def nameAvailable(name: String): DomainValidation[Boolean] = {
    nameAvailableMatcher(name, centreRepository, errMsgNameExists){ item =>
      item.name == name
    }
  }

  private def nameAvailable(name: String, excludeId: CentreId): DomainValidation[Boolean] = {
    nameAvailableMatcher(name, centreRepository, errMsgNameExists){ item =>
      (item.name == name) && (item.id != excludeId)
    }
  }

  /**
    * Utility method to validiate state of a centre
    */
  private def getDisabled(id: String): DomainValidation[DisabledCentre] = {
    centreRepository.getByKey(CentreId(id)).fold(
      err => DomainError(s"centre with id does not exist: $id").failureNel,
      centre => centre match {
        case centre: DisabledCentre => centre.success
        case centre => DomainError(s"centre is not disabled: $centre").failureNel
      }
    )
  }

  /**
    * Utility method to validiate state of a centre
    */
  private def getEnabled(id: String): DomainValidation[EnabledCentre] = {
    centreRepository.getByKey(CentreId(id)).fold(
      err => DomainError(s"centre with id does not exist: $id").failureNel,
      centre => centre match {
        case centre: EnabledCentre => centre.success
        case centre => DomainError(s"centre is not enabled: $centre").failureNel
      }
    )
  }

  private def updateCentre[T <: Centre]
    (cmd: CentreModifyCommand)
    (fn: Centre => DomainValidation[T])
      : DomainValidation[T] = {
    centreRepository.getByKey(CentreId(cmd.id)).fold(
      err => DomainError(s"centre with id does not exist: $id").failureNel,
      centre => for {
        validVersion  <-  centre.requireVersion(cmd.expectedVersion)
        updatedCentre <- fn(centre)
      } yield updatedCentre
    )
  }

  private def updateDisabled[T <: Centre]
    (cmd: CentreModifyCommand)
    (fn: DisabledCentre => DomainValidation[T])
      : DomainValidation[T] = {
    updateCentre(cmd) {
      case centre: DisabledCentre => fn(centre)
      case centre => DomainError(s"centre is not disabled: ${cmd.id}").failureNel
    }
  }

  private def updateEnabled[T <: Centre]
    (cmd: CentreModifyCommand)
    (fn: EnabledCentre => DomainValidation[T])
      : DomainValidation[T] = {
    updateCentre(cmd) {
      case centre: EnabledCentre => fn(centre)
      case centre => DomainError(s"centre is not enabled: ${cmd.id}").failureNel
    }
  }

  /**
   * Creates an event with the userId for the user that issued the command, and the current date and time.
   */
  private def createCentreEvent(id: CentreId, command: CentreCommand) =
    CentreEvent(id     = id.id,
                userId = command.userId,
                time   = Some(ISODateTimeFormat.dateTime.print(DateTime.now)))

  testData.addMultipleCentres
}
