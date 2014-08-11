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
      case other =>
        DomainError("invalid command received")
        ()
    }

    private def validateCmd(cmd: AddCentreCmd): DomainValidation[CentreAddedEvent] = {
      val centreId = centreRepository.nextIdentity

      if (centreRepository.getByKey(centreId).isSuccess) {
        throw new IllegalStateException(s"centre with id already exsits: $centreId")
      }

      for {
        nameAvailable <- nameAvailable(cmd.name)
        newCentre <- DisabledCentre.create(
          centreId, -1L, org.joda.time.DateTime.now, cmd.name, cmd.description)
        event <- CentreAddedEvent(
          newCentre.id.toString, newCentre.addedDate, newCentre.name, newCentre.description).success
      } yield event
    }

    private def validateCmd(cmd: UpdateCentreCmd): DomainValidation[CentreUpdatedEvent] = {
      val centreId = CentreId(cmd.id)
      for {
        nameAvailable <- nameAvailable(cmd.name, centreId)
        prevCentre <- isCentreDisabled(centreId)
        updatedCentre <- prevCentre.update(
          Some(cmd.expectedVersion), org.joda.time.DateTime.now, cmd.name, cmd.description)
        event <- CentreUpdatedEvent(
          cmd.id, updatedCentre.version, updatedCentre.lastUpdateDate.get, updatedCentre.name,
          updatedCentre.description).success
      } yield event
    }

    private def validateCmd(cmd: EnableCentreCmd): DomainValidation[CentreEnabledEvent] = {
      val centreId = CentreId(cmd.id)

      for {
        disabledCentre <- isCentreDisabled(centreId)
        enabledCentre <- disabledCentre.enable(Some(cmd.expectedVersion), org.joda.time.DateTime.now)
        event <- CentreEnabledEvent(
          centreId.id, enabledCentre.version, enabledCentre.lastUpdateDate.get).success
      } yield event
    }

    private def validateCmd(cmd: DisableCentreCmd): DomainValidation[CentreDisabledEvent] = {
      val centreId = CentreId(cmd.id)
      for {
        enabledCentre <- isCentreEnabled(centreId)
        disabledCentre <- enabledCentre.disable(Some(cmd.expectedVersion), org.joda.time.DateTime.now)
        event <- CentreDisabledEvent(
          cmd.id, disabledCentre.version, disabledCentre.lastUpdateDate.get).success
      } yield event
    }

    private def validateCmd(cmd: AddCentreLocationCmd): DomainValidation[CentreLocationAddedEvent] = {
      val centreId = CentreId(cmd.centreId)
      val locationId = locationRepository.nextIdentity

      for {
        disabledCentre <- isCentreDisabled(centreId)
        location <- Location(locationId, cmd.name, cmd.street, cmd.city, cmd.province,
            cmd.postalCode, cmd.poBoxNumber, cmd.countryIsoCode).success
        event <- CentreLocationAddedEvent(
          centreId.id, locationId.id, location.name, location.street, location.city,
          location.province, location.postalCode, location.poBoxNumber, location.countryIsoCode).success
      } yield event
    }

    private def validateCmd(cmd: RemoveCentreLocationCmd): DomainValidation[CentreLocationRemovedEvent] = {
      val centreId = CentreId(cmd.centreId)
      val locationId = LocationId(cmd.locationId);
      for {
        disabledCentre <- isCentreDisabled(centreId)
        centreLoc <- centreLocationRepository.getByKey(locationId)
        location <- locationRepository.getByKey(locationId)
        event <- CentreLocationRemovedEvent(cmd.centreId, cmd.locationId).success
      } yield event
    }

    private def validateCmd(cmd: AddCentreToStudyCmd): DomainValidation[CentreAddedToStudyEvent] = {
      val centreId = CentreId(cmd.centreId)
      val studyId = StudyId(cmd.studyId)
      for {
        disabledCentre <- isCentreDisabled(centreId)
        notExists <- centreStudyNotLinked(centreId, studyId)
        event <- CentreAddedToStudyEvent(cmd.centreId, cmd.studyId).success
      } yield event
    }

    private def validateCmd(cmd: RemoveCentreFromStudyCmd): DomainValidation[CentreRemovedFromStudyEvent] = {
      val centreId = CentreId(cmd.centreId)
      val studyId = StudyId(cmd.studyId);
      for {
        disabledCentre <- isCentreDisabled(centreId)
        item <- studyCentreRepository.withIds(studyId, centreId)
        event <- CentreRemovedFromStudyEvent(cmd.centreId, cmd.studyId).success
      } yield event
    }

    private def recoverEvent(event: CentreAddedEvent) {
      val centreId = CentreId(event.id)
      val validation = for {
        centre <- DisabledCentre.create(
          centreId, -1L, event.dateTime, event.name, event.description)
        savedCentre <- centreRepository.put(centre).success
      } yield centre

      if (validation.isFailure) {
        // this should never happen because the only way to get here is when the
        // command passed validation
        throw new IllegalStateException("recovering centre from event failed")
      }
    }

    private def recoverEvent(event: CentreUpdatedEvent) {
      val validation = for {
        disabledCentre <- isCentreDisabled(CentreId(event.id))
        updatedCentre <- disabledCentre.update(
          disabledCentre.versionOption, event.dateTime, event.name, event.description)
        savedCentre <- centreRepository.put(updatedCentre).success
      } yield savedCentre

      if (validation.isFailure) {
        // this should never happen because the only way to get here is when the
        // command passed validation
        throw new IllegalStateException("recovering centre from event failed")
      }
    }

    private def recoverEvent(event: CentreEnabledEvent) {
      val centreId = CentreId(event.id)
      val validation = for {
        disabledCentre <- isCentreDisabled(centreId)
        enabledCentre <- disabledCentre.enable(disabledCentre.versionOption, event.dateTime)
        savedCentre <- centreRepository.put(enabledCentre).success
      } yield  enabledCentre

      if (validation.isFailure) {
        // this should never happen because the only way to get here is when the
        // command passed validation
        throw new IllegalStateException("recovering centre from event failed")
      }
    }

    private def recoverEvent(event: CentreDisabledEvent) {
      val centreId = CentreId(event.id)
      val validation = for {
        enabledCentre <- isCentreEnabled(centreId)
        disabledCentre <- enabledCentre.disable(enabledCentre.versionOption, event.dateTime)
        savedCentre <- centreRepository.put(disabledCentre).success
      } yield disabledCentre

      if (validation.isFailure) {
        // this should never happen because the only way to get here is when the
        // command passed validation
        throw new IllegalStateException("recovering centre from event failed")
      }
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
        removed2 <- centreLocationRepository.remove(CentreLocation(centreId, locationId)).success
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
      studyCentreRepository.put(
        StudyCentre(studyCentreId, studyId, centreId))
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
    private def isCentreDisabled(centreId: CentreId): DomainValidation[DisabledCentre] = {
      centreRepository.getByKey(centreId).fold(
        err => DomainError(s"no centre with id: $centreId").failNel,
        centre => centre match {
          case dcentre: DisabledCentre => dcentre.success
          case _ => DomainError(s"centre is not disabled: ${centre.name}").failNel
        }
      )
    }

    /**
      * Utility method to validiate state of a centre
      */
    private def isCentreEnabled(centreId: CentreId): DomainValidation[EnabledCentre] = {
      centreRepository.getByKey(centreId).fold(
        err => DomainError(s"no centre with id: $centreId").failNel,
        centre => centre match {
          case enabledCentre: EnabledCentre => enabledCentre.success
          case _ => DomainError(s"centre is not enabled: ${centre.name}").failNel
        }
      )
    }

  }

}
