package org.biobank.services.participants

import akka.actor._
import akka.pattern.ask
import com.google.inject.ImplementedBy
import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Named, Singleton}
import org.biobank.domain.Slug
import org.biobank.domain.access._
import org.biobank.domain.participants._
import org.biobank.domain.studies._
import org.biobank.domain.users.UserId
import org.biobank.dto._
import org.biobank.infrastructure.commands.ParticipantCommands._
import org.biobank.infrastructure.events.ParticipantEvents._
import org.biobank.services._
import org.biobank.services.access.AccessService
import org.biobank.services.studies.StudiesService
import org.slf4j.{Logger, LoggerFactory}
import scala.concurrent._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@ImplementedBy(classOf[ParticipantsServiceImpl])
trait ParticipantsService extends BbwebService {

  def get(requestUserId: UserId,
          studyId:       StudyId,
          participantId: ParticipantId): ServiceValidation[ParticipantDto]

  def getBySlug(requestUserId: UserId, slug: Slug): ServiceValidation[ParticipantDto]

  def getByUniqueId(requestUserId: UserId,
                    studyId:       StudyId,
                    uniqueId:      String): ServiceValidation[ParticipantDto]

  def processCommand(cmd: ParticipantCommand): Future[ServiceValidation[ParticipantDto]]

  def snapshotRequest(requestUserId: UserId): ServiceValidation[Unit]

}

@SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
@Singleton
class ParticipantsServiceImpl @Inject() (
  @Named("participantsProcessor") val processor: ActorRef,
  val accessService:                             AccessService,
  val studiesService:                            StudiesService,
  val participantRepository:                     ParticipantRepository)
                                     (implicit executionContext: BbwebExecutionContext)
    extends ParticipantsService
    with AccessChecksSerivce
    with ServicePermissionChecks {

  import org.biobank.CommonValidations._
  import org.biobank.domain.access.AccessItem._

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  def get(requestUserId: UserId,
          studyId:       StudyId,
          participantId: ParticipantId): ServiceValidation[ParticipantDto] = {
    whenPermittedAndIsMember(requestUserId,
                             PermissionId.ParticipantRead,
                             Some(studyId),
                             None) { () =>
      for {
        participant <- participantRepository.withId(studyId, participantId)
        study       <- studiesService.getStudy(requestUserId, studyId)
      } yield participantToDto(participant, study)
    }
  }

  def getBySlug(requestUserId: UserId, slug: Slug): ServiceValidation[ParticipantDto] = {
    for {
      participant <- participantRepository.getBySlug(slug)
      study       <- studiesService.getStudy(requestUserId, participant.studyId)
      permission  <- accessService.hasPermissionAndIsMember(requestUserId,
                                                            PermissionId.ParticipantRead,
                                                            Some(study.id),
                                                            None)
      result <- {
        if (permission) participant.successNel[String]
        else Unauthorized.failureNel[Participant]
      }
    } yield participantToDto(participant, study)

  }

  def getByUniqueId(requestUserId: UserId,
                    studyId:       StudyId,
                    uniqueId:      String): ServiceValidation[ParticipantDto] = {
    whenPermittedAndIsMember(requestUserId,
                             PermissionId.ParticipantRead,
                             Some(studyId),
                             None) { () =>
      for {
        participant <- participantRepository.withUniqueId(studyId, uniqueId)
        study       <- studiesService.getStudy(requestUserId, studyId)
      } yield participantToDto(participant, study)
    }
  }

  def processCommand(cmd: ParticipantCommand): Future[ServiceValidation[ParticipantDto]] = {
    val validStudyId = cmd match {
        case c: AddParticipantCmd => StudyId(c.studyId).successNel[String]
        case c: ParticipantModifyCommand =>
          participantRepository.getByKey(ParticipantId(c.id)).map(p => p.studyId)
      }

    val permission = cmd match {
        case c: AddParticipantCmd => PermissionId.ParticipantCreate
        case c                    => PermissionId.ParticipantUpdate
      }

    val requestUserId = UserId(cmd.sessionUserId)

    validStudyId.fold(
      err => Future.successful(err.failure[ParticipantDto]),
      studyId => whenPermittedAndIsMemberAsync(requestUserId,
                                              permission,
                                              Some(studyId),
                                              None) { () =>
        ask(processor, cmd).mapTo[ServiceValidation[ParticipantEvent]].map { validation =>
          for {
            event       <- validation
            participant <- participantRepository.getByKey(ParticipantId(event.id))
            study       <- studiesService.getStudy(requestUserId, studyId)
          } yield participantToDto(participant, study)
        }
      }
    )
  }

  private def participantToDto(participant: Participant, study: Study): ParticipantDto = {
    ParticipantDto(id           = participant.id.id,
                   slug         = participant.slug,
                   study        = EntityInfoDto(study),
                   version      = participant.version,
                   timeAdded    = participant.timeAdded.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                   timeModified = participant.timeModified.map(_.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)),
                   uniqueId     = participant.uniqueId,
                   annotations  = participant.annotations)
  }

}
