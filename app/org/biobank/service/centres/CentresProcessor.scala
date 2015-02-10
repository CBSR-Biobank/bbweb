package org.biobank.service.centre

import org.biobank.service.{ Processor, WrappedCommand, WrappedEvent }
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

import akka.actor. { ActorRef, Props }
import org.joda.time.DateTime
import akka.pattern.ask
import org.slf4j.LoggerFactory
import akka.persistence.SnapshotOffer
import scaldi.akka.AkkaInjectable
import scaldi.{Injectable, Injector}
import scalaz._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

class CentresProcessor(implicit inj: Injector) extends Processor with AkkaInjectable {

  override def persistenceId = "centre-processor-id"

  case class SnapshotState(centres: Set[Centre])

  val centreRepository = inject [CentreRepository]

  val locationRepository = inject [LocationRepository]

  val centreStudiesRepository = inject [CentreStudiesRepository]

  val centreLocationsRepository = inject [CentreLocationsRepository]

  val errMsgNameExists = "centre with name already exists"

  val receiveRecover: Receive = {
    case wevent: WrappedEvent[_] =>
      wevent.event match {
        case event: CentreAddedEvent            => recoverCentreAddedEvent(event, wevent.userId, wevent.dateTime)
        case event: CentreUpdatedEvent          => recoverCentreUpdatedEvent(event, wevent.userId, wevent.dateTime)
        case event: CentreEnabledEvent          => recoverCentreEnabledEvent(event, wevent.userId, wevent.dateTime)
        case event: CentreDisabledEvent         => recoverCentreDisabledEvent(event, wevent.userId, wevent.dateTime)
        case event: CentreLocationAddedEvent    => recoverCentreLocationAddedEvent(event, wevent.userId, wevent.dateTime)
        case event: CentreLocationRemovedEvent  => recoverCentreLocationRemovedEvent(event, wevent.userId, wevent.dateTime)
        case event: CentreAddedToStudyEvent     => recoverCentreAddedToStudyEvent(event, wevent.userId, wevent.dateTime)
        case event: CentreRemovedFromStudyEvent => recoverCentreRemovedFromStudyEvent(event, wevent.userId, wevent.dateTime)

        case event => log.error(s"event not handled: $event")
      }

    case SnapshotOffer(_, snapshot: SnapshotState) =>
      snapshot.centres.foreach{ centre => centreRepository.put(centre) }
  }

  val receiveCommand: Receive = {
    case procCmd: WrappedCommand =>
      implicit val userId = procCmd.userId
      procCmd.command match {
        case cmd: AddCentreCmd             => processAddCentreCmd(cmd)
        case cmd: UpdateCentreCmd          => processUpdateCentreCmd(cmd)
        case cmd: EnableCentreCmd          => processEnableCentreCmd(cmd)
        case cmd: DisableCentreCmd         => processDisableCentreCmd(cmd)
        case cmd: AddCentreLocationCmd     => processAddCentreLocationCmd(cmd)
        case cmd: RemoveCentreLocationCmd  => processRemoveCentreLocationCmd(cmd)
        case cmd: AddStudyToCentreCmd      => processAddStudyToCentreCmd(cmd)
        case cmd: RemoveStudyFromCentreCmd => processRemoveStudyFromCentreCmd(cmd)
      }

    case "snap" =>
      saveSnapshot(SnapshotState(centreRepository.getValues.toSet))
      stash()

    case cmd => log.error(s"CentresProcessor: message not handled: $cmd")

  }

  private def processAddCentreCmd(cmd: AddCentreCmd)(implicit userId: Option[UserId]): Unit = {
    val timeNow = DateTime.now
    val centreId = centreRepository.nextIdentity

    if (centreRepository.getByKey(centreId).isSuccess) {
      log.error(s"centre with id already exsits: $centreId")
    }

    val event = for {
      nameAvailable <- nameAvailable(cmd.name)
      newCentre <- DisabledCentre.create(centreId, -1L, timeNow, cmd.name, cmd.description)
      event <- CentreAddedEvent(
        id = newCentre.id.id,
        name = Some(newCentre.name),
        description = newCentre.description).success
    } yield event

    process(event){ wevent =>
      recoverCentreAddedEvent(wevent.event, wevent.userId, wevent.dateTime)
    }
  }

  private def processUpdateCentreCmd(cmd: UpdateCentreCmd)(implicit userId: Option[UserId]): Unit = {
    val v = updateDisabled(cmd) { centre =>
      for {
        nameAvailable <- nameAvailable(cmd.name, centre.id)
        updatedCentre <- centre.update(cmd.name, cmd.description)
      } yield updatedCentre
    }

    val event = v.fold(
      err => err.failure,
      centre => CentreUpdatedEvent(
        id = cmd.id,
        version = Some(centre.version),
        name = Some(centre.name),
        description = centre.description).success
    )

    process(event){ wevent =>
      recoverCentreUpdatedEvent(wevent.event, wevent.userId, wevent.dateTime)
    }
  }

  private def processEnableCentreCmd(cmd: EnableCentreCmd)(implicit userId: Option[UserId]): Unit = {
    val timeNow = DateTime.now
    val v = updateDisabled(cmd) { c => c.enable }

    val  event = v.fold(
      err => err.failure,
      centre => CentreEnabledEvent(id = cmd.id, version = Some(centre.version)).success
    )

    process(event){ wevent =>
      recoverCentreEnabledEvent(wevent.event, wevent.userId, wevent.dateTime)
    }
  }

  private def processDisableCentreCmd(cmd: DisableCentreCmd)(implicit userId: Option[UserId]): Unit = {
    val v = updateEnabled(cmd) { c => c.disable }
    val event = v.fold(
      err => err.failure,
      centre => CentreDisabledEvent(id = cmd.id, version = Some(centre.version)).success
    )

    process(event){ wevent =>
      recoverCentreDisabledEvent(wevent.event, wevent.userId, wevent.dateTime)
    }
  }

  private def processAddCentreLocationCmd(cmd: AddCentreLocationCmd)(implicit userId: Option[UserId]): Unit = {
    val locationId = locationRepository.nextIdentity
    val event = for {
      centre <- getDisabled(cmd.centreId)
      location <- Location(locationId, cmd.name, cmd.street, cmd.city, cmd.province,
        cmd.postalCode, cmd.poBoxNumber, cmd.countryIsoCode).success
      event <- CentreLocationAddedEvent(
        centreId       = cmd.centreId,
        locationId     = locationId.id,
        name           = Some(cmd.name),
        street         = Some(cmd.street),
        city           = Some(cmd.city),
        province       = Some(cmd.province),
        postalCode     = Some(cmd.postalCode),
        poBoxNumber    = cmd.poBoxNumber,
        countryIsoCode = Some(cmd.countryIsoCode)).success
    } yield event

    process(event){ wevent =>
      recoverCentreLocationAddedEvent(wevent.event, wevent.userId, wevent.dateTime)
    }
  }

  private def processRemoveCentreLocationCmd(cmd: RemoveCentreLocationCmd)(implicit userId: Option[UserId]): Unit = {
    val event = locationRepository.getByKey(LocationId(cmd.locationId)).fold(
      err => DomainError(s"location with id does not exist: $id").failureNel,
      location => for {
        centre <- getDisabled(cmd.centreId)
        event <- CentreLocationRemovedEvent(cmd.centreId, cmd.locationId).success
      } yield event
    )

    process(event){ wevent =>
      recoverCentreLocationRemovedEvent(wevent.event, wevent.userId, wevent.dateTime)
    }
  }

  private def processAddStudyToCentreCmd(cmd: AddStudyToCentreCmd)(implicit userId: Option[UserId]): Unit = {
    val event = for {
      centre <- getDisabled(cmd.centreId)
      notLinked <- centreStudyNotLinked(centre.id, StudyId(cmd.studyId))
      event <- CentreAddedToStudyEvent(cmd.centreId, cmd.studyId).success
    } yield event

    process(event){ wevent =>
      recoverCentreAddedToStudyEvent(wevent.event, wevent.userId, wevent.dateTime)
    }
  }

  private def processRemoveStudyFromCentreCmd(cmd: RemoveStudyFromCentreCmd)(implicit userId: Option[UserId]): Unit = {
    val event = for {
      centre <- getDisabled(cmd.centreId)
      item <- centreStudiesRepository.withIds(StudyId(cmd.studyId), centre.id)
      event <- CentreRemovedFromStudyEvent(cmd.centreId, cmd.studyId).success
    } yield event

    process(event){ wevent =>
      recoverCentreRemovedFromStudyEvent(wevent.event, wevent.userId, wevent.dateTime)
    }
  }

  private def recoverCentreAddedEvent
    (event: CentreAddedEvent, userId: Option[UserId], dateTime: DateTime)
      : Unit = {
    centreRepository.put(DisabledCentre(
      CentreId(event.id), 0L, dateTime, None, event.getName, event.description))
    ()
  }

  private def recoverCentreUpdatedEvent
    (event: CentreUpdatedEvent, userId: Option[UserId], dateTime: DateTime)
      : Unit = {
    getDisabled(event.id).fold(
      err => log.error(s"updating centre from event failed: $err"),
      centre => {
        centreRepository.put(centre.copy(
          version      = event.getVersion,
          timeModified = Some(dateTime),
          name         = event.getName,
          description  = event.description))
        ()
      }
    )
  }

  private def recoverCentreEnabledEvent
    (event: CentreEnabledEvent, userId: Option[UserId], dateTime: DateTime)
      : Unit = {
    getDisabled(event.id).fold(
      err => log.error(s"enabling centre from event failed: $err"),
      centre => {
        centreRepository.put(EnabledCentre(
          CentreId(event.id), event.getVersion, centre.timeAdded, Some(dateTime),
          centre.name, centre.description))
        ()
      }
    )
  }

  private def recoverCentreDisabledEvent
    (event: CentreDisabledEvent, userId: Option[UserId], dateTime: DateTime)
      : Unit = {
    getEnabled(event.id).fold(
      err => log.error(s"disabling centre from event failed: $err"),
      centre => {
        centreRepository.put(DisabledCentre(
          CentreId(event.id), event.getVersion, centre.timeAdded, Some(dateTime),
          centre.name, centre.description))
        ()
      }
    )
  }

  private def recoverCentreLocationAddedEvent
    (event: CentreLocationAddedEvent, userId: Option[UserId], dateTime: DateTime)
      : Unit = {
    val centreId = CentreId(event.centreId)
    val locationId = LocationId(event.locationId)

    locationRepository.put(Location(
     id             = locationId,
     name           = event.getName,
     street         = event.getStreet,
     city           = event.getCity,
     province       = event.getProvince,
     postalCode     = event.getPostalCode,
     poBoxNumber    = event.poBoxNumber,
     countryIsoCode = event.getCountryIsoCode))
    centreLocationsRepository.put(CentreLocation(centreId, locationId))
    ()
  }

  private def recoverCentreLocationRemovedEvent
    (event: CentreLocationRemovedEvent, userId: Option[UserId], dateTime: DateTime)
      : Unit = {
    val centreId = CentreId(event.centreId)
    val locationId = LocationId(event.locationId)
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
  }

  private def recoverCentreAddedToStudyEvent
    (event: CentreAddedToStudyEvent, userId: Option[UserId], dateTime: DateTime)
      : Unit = {
    val centreId = CentreId(event.centreId)
    val studyId = StudyId(event.studyId)
    val studyCentreId = centreStudiesRepository.nextIdentity
    centreStudiesRepository.put(StudyCentre(studyCentreId, studyId, centreId))
    ()
  }

  private def recoverCentreRemovedFromStudyEvent
    (event: CentreRemovedFromStudyEvent, userId: Option[UserId], dateTime: DateTime)
      : Unit = {
    val centreId = CentreId(event.centreId)
    val studyId = StudyId(event.studyId)
    val studyCentreId = centreStudiesRepository.nextIdentity
    val validation = for {
      studyCentre <- centreStudiesRepository.withIds(studyId, centreId)
      removed <- centreStudiesRepository.remove(studyCentre).success
    } yield removed

    if (validation.isFailure) {
      // this should never happen because the only way to get here is when the
      // command passed validation
      log.error(s"removing study centre link with event failed: $validation")
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

}
