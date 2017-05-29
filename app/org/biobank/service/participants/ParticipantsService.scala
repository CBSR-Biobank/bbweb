package org.biobank.service.participants

import akka.actor._
import akka.pattern.ask
import com.google.inject.ImplementedBy
import javax.inject.{Inject, Named, Singleton}
import org.biobank.domain.access._
import org.biobank.domain.participants._
import org.biobank.domain.study._
import org.biobank.domain.user.UserId
import org.biobank.infrastructure.command.ParticipantCommands._
import org.biobank.infrastructure.event.ParticipantEvents._
import org.biobank.service._
import org.biobank.service.access.AccessService
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@ImplementedBy(classOf[ParticipantsServiceImpl])
trait ParticipantsService extends BbwebService {

  def get(requestUserId: UserId,
          studyId:       StudyId,
          participantId: ParticipantId): ServiceValidation[Participant]

  def getByUniqueId(requestUserId: UserId,
                    studyId:       StudyId,
                    uniqueId:      String): ServiceValidation[Participant]

  def processCommand(cmd: ParticipantCommand): Future[ServiceValidation[Participant]]

  def snapshotRequest(requestUserId: UserId): ServiceValidation[Unit]

}

@Singleton
class ParticipantsServiceImpl @Inject() (
  @Named("participantsProcessor") val processor: ActorRef,
  val accessService:                             AccessService,
  val studyRepository:                           StudyRepository,
  val participantRepository:                     ParticipantRepository,
  val collectionEventRepository:                 CollectionEventRepository)
    extends ParticipantsService
    with AccessChecksSerivce
    with ServicePermissionChecks {

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  def get(requestUserId: UserId,
          studyId:       StudyId,
          participantId: ParticipantId): ServiceValidation[Participant] = {
    whenPermittedAndIsMember(requestUserId,
                             PermissionId.ParticipantRead,
                             Some(studyId),
                             None) { () =>
      participantRepository.withId(studyId, participantId)
    }
  }

  def getByUniqueId(requestUserId: UserId,
                    studyId:       StudyId,
                    uniqueId:      String): ServiceValidation[Participant] = {
    whenPermittedAndIsMember(requestUserId,
                             PermissionId.ParticipantRead,
                             Some(studyId),
                             None) { () =>
      participantRepository.withUniqueId(studyId, uniqueId)
    }
  }

  def processCommand(cmd: ParticipantCommand): Future[ServiceValidation[Participant]] = {
    val validStudyId = cmd match {
        case c: AddParticipantCmd => StudyId(c.studyId).successNel[String]
        case c: ParticipantModifyCommand =>
          participantRepository.getByKey(ParticipantId(c.id)).map(p => p.studyId)
      }

    val permission = cmd match {
        case c: AddParticipantCmd => PermissionId.ParticipantCreate
        case c                    => PermissionId.ParticipantUpdate
      }

    validStudyId.fold(
      err => Future.successful(err.failure[Participant]),
      studyId => whenPermittedAndIsMemberAsync(UserId(cmd.sessionUserId),
                                               permission,
                                               Some(studyId),
                                               None) { () =>
        ask(processor, cmd).mapTo[ServiceValidation[ParticipantEvent]].map { validation =>
          for {
            event       <- validation
            participant <- participantRepository.getByKey(ParticipantId(event.id))
          } yield participant
        }
      }
    )
  }

}
