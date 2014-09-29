package org.biobank.service.centre

import org.biobank.service.Processor
import org.biobank.domain.centre._
import org.biobank.domain.study.StudyId
import org.biobank.domain.{
  DomainValidation,
  DomainError,
  Location,
  LocationId,
  RepositoriesComponent,
  RepositoriesComponentImpl
}
import org.biobank.domain.user.UserId
import org.biobank.infrastructure.command.CentreCommands._
import org.biobank.infrastructure.event.CentreEvents._

import akka.actor. { ActorRef, Props }
import org.joda.time.DateTime
import akka.pattern.ask
import org.slf4j.LoggerFactory
import akka.persistence.SnapshotOffer
import scalaz._
import scalaz.Scalaz._

trait CentresProcessorComponent {
  self: RepositoriesComponent =>

  /**
    * An actor that processes commands related to the [[org.biobank.domain.centre.Centre]] aggregate root.
    *
    * This implementation uses Akka persistence.
    */
  sealed class CentresProcessor extends Processor {

    override def persistenceId = "centre-processor-id"

    case class SnapshotState(centres: Set[Centre])

    val errMsgNameExists = "centre with name already exists"

    val receiveRecover: Receive = {
      case event: CentreAddedEvent =>            recoverEvent(event)
      case event: CentreUpdatedEvent =>          recoverEvent(event)
      case event: CentreEnabledEvent =>          recoverEvent(event)
      case event: CentreDisabledEvent =>         recoverEvent(event)
      case event: CentreLocationAddedEvent =>    recoverEvent(event)
      case event: CentreLocationRemovedEvent =>  recoverEvent(event)
      case event: CentreAddedToStudyEvent =>     recoverEvent(event)
      case event: CentreRemovedFromStudyEvent => recoverEvent(event)

      case SnapshotOffer(_, snapshot: SnapshotState) =>
        snapshot.centres.foreach{ centre => centreRepository.put(centre) }
    }

    val receiveCommand: Receive = {
      case cmd: AddCentreCmd =>             process(validateCmd(cmd)){ event => recoverEvent(event) }
      case cmd: UpdateCentreCmd =>          process(validateCmd(cmd)){ event => recoverEvent(event) }
      case cmd: EnableCentreCmd =>          process(validateCmd(cmd)){ event => recoverEvent(event) }
      case cmd: DisableCentreCmd =>         process(validateCmd(cmd)){ event => recoverEvent(event) }
      case cmd: AddCentreLocationCmd =>     process(validateCmd(cmd)){ event => recoverEvent(event) }
      case cmd: RemoveCentreLocationCmd =>  process(validateCmd(cmd)){ event => recoverEvent(event) }
      case cmd: AddCentreToStudyCmd =>      process(validateCmd(cmd)){ event => recoverEvent(event) }
      case cmd: RemoveCentreFromStudyCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

      case "snap" =>
        saveSnapshot(SnapshotState(centreRepository.getValues.toSet))
        stash()

      case cmd => log.error(s"message not handled: $cmd")

    }

    private def validateCmd(cmd: AddCentreCmd): DomainValidation[CentreAddedEvent] = {
      val timeNow = DateTime.now
      val centreId = centreRepository.nextIdentity

      if (centreRepository.getByKey(centreId).isSuccess) {
        throw new IllegalStateException(s"centre with id already exsits: $centreId")
      }

      for {
        nameAvailable <- nameAvailable(cmd.name)
        newCentre <- DisabledCentre.create(centreId, -1L, timeNow, cmd.name, cmd.description)
        event <- CentreAddedEvent(newCentre.id.id, timeNow, newCentre.name, newCentre.description).success
      } yield event
    }

    private def validateCmd(cmd: UpdateCentreCmd): DomainValidation[CentreUpdatedEvent] = {
      val timeNow = DateTime.now
      val v = updateDisabled(cmd) { centre =>
        for {
          nameAvailable <- nameAvailable(cmd.name, centre.id)
          updatedCentre <- centre.update(cmd.name, cmd.description)
        } yield updatedCentre
      }

      v.fold(
        err => DomainError(s"error $err occurred on $cmd").failNel,
        centre => CentreUpdatedEvent(cmd.id, centre.version, timeNow, centre.name, centre.description).success
      )
    }

    private def validateCmd(cmd: EnableCentreCmd): DomainValidation[CentreEnabledEvent] = {
      val timeNow = DateTime.now
      val v = updateDisabled(cmd) { c => c.enable }

      v.fold(
        err => DomainError(s"error $err occurred on $cmd").failNel,
        centre => CentreEnabledEvent(cmd.id, centre.version, timeNow).success
      )
    }

    private def validateCmd(cmd: DisableCentreCmd): DomainValidation[CentreDisabledEvent] = {
      val timeNow = DateTime.now
      val v = updateEnabled(cmd) { c => c.disable }
      v.fold(
        err => DomainError(s"error $err occurred on $cmd").failNel,
        centre => CentreDisabledEvent(cmd.id, centre.version, timeNow).success
      )
    }

    private def validateCmd(cmd: AddCentreLocationCmd): DomainValidation[CentreLocationAddedEvent] = {
      val locationId = locationRepository.nextIdentity
      for {
        centre <- getDisabled(cmd.centreId)
        location <- Location(locationId, cmd.name, cmd.street, cmd.city, cmd.province,
          cmd.postalCode, cmd.poBoxNumber, cmd.countryIsoCode).success
        event <- CentreLocationAddedEvent(
          cmd.centreId, locationId.id, cmd.name, cmd.street, cmd.city, cmd.province,
          cmd.postalCode, cmd.poBoxNumber, cmd.countryIsoCode).success
      } yield event
    }

    private def validateCmd(cmd: RemoveCentreLocationCmd): DomainValidation[CentreLocationRemovedEvent] = {

      locationRepository.getByKey(LocationId(cmd.locationId)).fold(
        err => DomainError(s"no location with id: $id").failNel,
        location => for {
          centre <- getDisabled(cmd.centreId)
          event <- CentreLocationRemovedEvent(cmd.centreId, cmd.locationId).success
        } yield event
      )
    }

    private def validateCmd(cmd: AddCentreToStudyCmd): DomainValidation[CentreAddedToStudyEvent] = {
      for {
        centre <- getDisabled(cmd.centreId)
        notLinked <- centreStudyNotLinked(centre.id, StudyId(cmd.studyId))
        event <- CentreAddedToStudyEvent(cmd.centreId, cmd.studyId).success
      } yield event
    }

    private def validateCmd(cmd: RemoveCentreFromStudyCmd)
        : DomainValidation[CentreRemovedFromStudyEvent] = {
      for {
        centre <- getDisabled(cmd.centreId)
        item <- studyCentreRepository.withIds(StudyId(cmd.studyId), centre.id)
        event <- CentreRemovedFromStudyEvent(cmd.centreId, cmd.studyId).success
      } yield event
    }

    private def recoverEvent(event: CentreAddedEvent) {
      centreRepository.put(DisabledCentre(
          CentreId(event.id), 0L, event.dateTime, None, event.name, event.description))
      ()
    }

    private def recoverEvent(event: CentreUpdatedEvent) {
      getDisabled(event.id).fold(
        err => throw new IllegalStateException(s"updating centre from event failed: $err"),
        centre => centreRepository.put(centre.copy(
          version = event.version,
          timeModified = Some(event.dateTime),
          name = event.name,
          description = event.description))
      )
      ()
    }

    private def recoverEvent(event: CentreEnabledEvent) {
      getDisabled(event.id).fold(
        err => throw new IllegalStateException(s"enabling centre from event failed: $err"),
        centre => centreRepository.put(EnabledCentre(
          CentreId(event.id), event.version, centre.timeAdded, Some(event.dateTime),
          centre.name, centre.description))
      )
      ()
    }

    private def recoverEvent(event: CentreDisabledEvent) {
      getEnabled(event.id).fold(
        err => throw new IllegalStateException(s"disabling centre from event failed: $err"),
        centre => centreRepository.put(DisabledCentre(
          CentreId(event.id), event.version, centre.timeAdded, Some(event.dateTime),
          centre.name, centre.description))
      )
      ()
    }

    private def recoverEvent(event: CentreLocationAddedEvent) {
      val centreId = CentreId(event.centreId)
      val locationId = LocationId(event.locationId)
      locationRepository.put(
        Location(locationId, event.name, event.street, event.city, event.province,
          event.postalCode, event.poBoxNumber, event.countryIsoCode))
      centreLocationRepository.put(CentreLocation(centreId, locationId))
      ()
    }

    private def recoverEvent(event: CentreLocationRemovedEvent) {
      val centreId = CentreId(event.centreId)
      val locationId = LocationId(event.locationId)
      val validation = for {
        location <- locationRepository.getByKey(locationId)
        removed <- locationRepository.remove(location).success
        link <- centreLocationRepository.remove(CentreLocation(centreId, locationId)).success
      } yield location

      if (validation.isFailure) {
        // this should never happen because the only way to get here is when the
        // command passed validation
        throw new IllegalStateException("removing centre location with event failed")
      }
    }

    private def recoverEvent(event: CentreAddedToStudyEvent) {
      val centreId = CentreId(event.centreId)
      val studyId = StudyId(event.studyId)
      val studyCentreId = studyCentreRepository.nextIdentity
      studyCentreRepository.put(StudyCentre(studyCentreId, studyId, centreId))
      ()
    }

    private def recoverEvent(event: CentreRemovedFromStudyEvent) {
      val centreId = CentreId(event.centreId)
      val studyId = StudyId(event.studyId)
      val studyCentreId = studyCentreRepository.nextIdentity
      val validation = for {
        studyCentre <- studyCentreRepository.withIds(studyId, centreId)
        removed <- studyCentreRepository.remove(studyCentre).success
      } yield removed

      if (validation.isFailure) {
        // this should never happen because the only way to get here is when the
        // command passed validation
        throw new IllegalStateException(s"removing study centre link with event failed: $validation")
      }
    }

    /** Returns true if the centre and study are not already linked.
      *
      */
    private def centreStudyNotLinked(centreId: CentreId, studyId: StudyId): DomainValidation[Boolean] = {
      val exists = studyCentreRepository.getValues.exists { x =>
        (x.centreId == centreId) && (x.studyId == studyId)
      }
      if (exists) {
        DomainError(s"centre and study already linked: { centreId: $centreId, studyId: $studyId }").failNel
      } else {
        true.success
      }
    }

    private def nameAvailable(name: String): DomainValidation[Boolean] = {
      nameAvailableMatcher(name, centreRepository, errMsgNameExists){ item =>
        item.name.equals(name)
      }
    }

    private def nameAvailable(name: String, excludeId: CentreId): DomainValidation[Boolean] = {
      nameAvailableMatcher(name, centreRepository, errMsgNameExists){ item =>
        item.name.equals(name) && (item.id != excludeId)
      }
    }

    /**
      * Utility method to validiate state of a centre
      */
    private def getDisabled(id: String): DomainValidation[DisabledCentre] = {
      centreRepository.getByKey(CentreId(id)).fold(
        err => DomainError(s"no centre with id: $id").failNel,
        centre => centre match {
          case centre: DisabledCentre => centre.success
          case centre => DomainError(s"centre is not disabled: $centre").failNel
        }
      )
    }

    /**
      * Utility method to validiate state of a centre
      */
    private def getEnabled(id: String): DomainValidation[EnabledCentre] = {
      centreRepository.getByKey(CentreId(id)).fold(
        err => DomainError(s"no centre with id: $id").failNel,
        centre => centre match {
          case centre: EnabledCentre => centre.success
          case centre => DomainError(s"centre is not enabled: $centre").failNel
        }
      )
    }

    private def updateCentre[T <: Centre]
      (cmd: CentreCommand)
      (fn: Centre => DomainValidation[T])
        : DomainValidation[T] = {
      centreRepository.getByKey(CentreId(cmd.id)).fold(
        err => DomainError(s"no centre with id: $id").failNel,
        centre => for {
          validVersion  <-  centre.requireVersion(cmd.expectedVersion)
          updatedCentre <- fn(centre)
        } yield updatedCentre
      )
    }

    private def updateDisabled[T <: Centre]
      (cmd: CentreCommand)
      (fn: DisabledCentre => DomainValidation[T])
        : DomainValidation[T] = {
      updateCentre(cmd) {
        case centre: DisabledCentre => fn(centre)
        case centre => s"$centre for $cmd is not disabled".failNel
      }
    }

    private def updateEnabled[T <: Centre]
      (cmd: CentreCommand)
      (fn: EnabledCentre => DomainValidation[T])
        : DomainValidation[T] = {
      updateCentre(cmd) {
        case centre: EnabledCentre => fn(centre)
        case centre => s"$centre for $cmd is not enabled".failNel
      }
    }

  }

}
