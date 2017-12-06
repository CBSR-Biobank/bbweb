package org.biobank.service.studies

import akka.actor._
import akka.pattern.ask
import com.google.inject.ImplementedBy
import javax.inject._
import org.biobank.domain.access._
import org.biobank.domain.centre.{Centre, CentreRepository}
import org.biobank.domain.participants.CollectionEventRepository
import org.biobank.domain.study._
import org.biobank.domain.user.UserId
//import org.biobank.dto._
import org.biobank.infrastructure._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.ProcessingTypeEvents._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.service._
import org.biobank.service.access.AccessService
import org.biobank.service.centres.{CentreServicePermissionChecks, CentreLocation}
import org.slf4j.{Logger, LoggerFactory}
import scala.concurrent._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@ImplementedBy(classOf[StudiesServiceImpl])
trait StudiesService extends BbwebService {

  /**
   * Returns a sequence of enabled [[Study]] for which the user can collect specimens for.
   *
   * @param requestUserId the ID of the user making the request.
   *
   * @param filter the string representation of the filter expression to use to filter the studies.
   *
   * @param sort the string representation of the sort expression to use when sorting the studies.
   */
  def collectionStudies(requestUserId: UserId, filter: FilterString, sort: SortString)
      : ServiceValidation[Seq[Study]]

  def getStudyCount(requestUserId: UserId): ServiceValidation[Long]

  def getCountsByStatus(requestUserId: UserId): ServiceValidation[StudyCountsByStatus]

  /**
   * Returns a set of studies. The entities can be filtered and or sorted using expressions.
   *
   * @param filter the string representation of the filter expression to use to filter the studies.
   *
   * @param sort the string representation of the sort expression to use when sorting the studies.
   */
  def getStudies(requestUserId: UserId,
                 filter:        FilterString,
                 sort:          SortString): ServiceValidation[Seq[Study]]

  def getStudy(requestUserId: UserId, studyId: StudyId): ServiceValidation[Study]

  def getStudyBySlug(requestUserId: UserId, slug: String): ServiceValidation[Study]

  def getCentresForStudy(requestUserId: UserId, studyId: StudyId): ServiceValidation[Set[CentreLocation]]

  def enableAllowed(requestUserId: UserId, studyId: StudyId): ServiceValidation[Boolean]

  def processingTypeWithId(studyId: StudyId, processingTypeId: ProcessingTypeId)
      : ServiceValidation[ProcessingType]

  def processingTypesForStudy(studyId: StudyId): ServiceValidation[Set[ProcessingType]]

  def specimenLinkTypeWithId(processingTypeId: ProcessingTypeId,
                             specimenLinkTypeId: SpecimenLinkTypeId)
      : ServiceValidation[SpecimenLinkType]

  def specimenLinkTypesForProcessingType(processingTypeId: ProcessingTypeId)
      : ServiceValidation[Set[SpecimenLinkType]]

  //def getProcessingDto(studyId: StudyId): ServiceValidation[ProcessingDto]

  def processCommand(cmd: StudyCommand): Future[ServiceValidation[Study]]

  // FIXME: move to its own service
  def processProcessingTypeCommand(cmd: StudyCommand)
      : Future[ServiceValidation[ProcessingType]]

  // FIXME: move to its own service
  def processRemoveProcessingTypeCommand(cmd: StudyCommand)
      : Future[ServiceValidation[Boolean]]

  def snapshotRequest(requestUserId: UserId): ServiceValidation[Unit]

}

/**
  * This is the Study Aggregate Application Service.
  *
  * Handles the commands to configure studies. the commands are forwarded to the Study Aggregate
  * Processor.
  *
  * @param studiesProcessor
  *
 */
@SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
class StudiesServiceImpl @Inject()(
  @Named("studiesProcessor") val processor: ActorRef,
  val accessService:                        AccessService,
  val studyRepository:                      StudyRepository,
  val centreRepository:                     CentreRepository,
  val processingTypeRepository:             ProcessingTypeRepository,
  val specimenGroupRepository:              SpecimenGroupRepository,
  val collectionEventTypeRepository:        CollectionEventTypeRepository,
  val collectionEventRepository:            CollectionEventRepository,
  val specimenLinkTypeRepository:           SpecimenLinkTypeRepository)
                               (implicit executionContext: BbwebExecutionContext)
    extends StudiesService
    with AccessChecksSerivce
    with StudyServicePermissionChecks
    with CentreServicePermissionChecks {

  import org.biobank.CommonValidations._
  import org.biobank.domain.access.AccessItem._

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  def collectionStudies(requestUserId: UserId, filter: FilterString, sort: SortString)
      : ServiceValidation[Seq[Study]] = {
    val collectionStudies: ServiceValidation[Seq[Study]] = for {
        membershipStudies <- getMembershipStudies(requestUserId)
        membershipCentres <- getMembershipCentres(requestUserId)
      } yield {
        val enabledStudies = membershipStudies.filter { s => s.state == Study.enabledState }
        val enabledCentres = membershipCentres.filter { c => c.state == Centre.enabledState }
        val centreStudies  = enabledCentres
          .flatMap { centre =>
            centre.studyIds.map(studyRepository.getByKey)
          }
          .toList.sequenceU
          .map { s =>
            s.filter { s => s.state == Study.enabledState }.toSet
          }

        // centreStudies should never be an error because its value is based on enabled centres
        centreStudies.fold(
          err => Seq.empty[EnabledStudy],
          cs  => (enabledStudies & cs).toSeq
        )
      }
    collectionStudies.flatMap(studies => filterStudiesInternal(studies.toSet, filter, sort))
  }

  def getStudyCount(requestUserId: UserId): ServiceValidation[Long] = {
    withPermittedStudies(requestUserId) { studies =>
      studies.size.toLong.successNel[String]
    }
  }

  def getCountsByStatus(requestUserId: UserId): ServiceValidation[StudyCountsByStatus] = {
    withPermittedStudies(requestUserId) { studies =>
      StudyCountsByStatus(
        total         = studies.size.toLong,
        disabledCount = studies.collect { case s: DisabledStudy => s }.size.toLong,
        enabledCount  = studies.collect { case s: EnabledStudy => s }.size.toLong,
        retiredCount  = studies.collect { case s: RetiredStudy => s }.size.toLong
      ).successNel[String]
    }
  }

  def getStudies(requestUserId: UserId, filter: FilterString, sort: SortString)
      : ServiceValidation[Seq[Study]] = {
    withPermittedStudies(requestUserId) { studies =>
      filterStudiesInternal(studies, filter, sort)
    }
  }

  def getStudy(requestUserId: UserId, studyId: StudyId) : ServiceValidation[Study] = {
    whenPermittedAndIsMember(requestUserId,
                             PermissionId.StudyRead,
                             Some(studyId),
                             None) { () =>
      studyRepository.getByKey(studyId)
    }
  }

  def getStudyBySlug(requestUserId: UserId, slug: String): ServiceValidation[Study] = {
    for {
      study      <- studyRepository.getBySlug(slug)
      permission <- accessService.hasPermissionAndIsMember(requestUserId,
                                                           PermissionId.StudyRead,
                                                           Some(study.id),
                                                           None)
      result     <- if (permission) study.successNel[String] else Unauthorized.failureNel[Study]
    } yield result
  }

  def getCentresForStudy(requestUserId: UserId,
                         studyId:       StudyId): ServiceValidation[Set[CentreLocation]] = {
    whenPermittedAndIsMember(requestUserId,
                             PermissionId.StudyRead,
                             Some(studyId),
                             None) { () =>
      centreRepository.withStudy(studyId).flatMap { centre =>
        centre.locations.map { location =>
          CentreLocation(centre.id.id, location.id.id, centre.name, location.name)
        }
      }.successNel[String]
    }
  }

  def enableAllowed(requestUserId: UserId, studyId: StudyId): ServiceValidation[Boolean] = {
    whenPermittedAndIsMember(requestUserId,
                             PermissionId.StudyRead,
                             Some(studyId),
                             None) { () =>
      val specimenDescriptions = collectionEventTypeRepository.allForStudy(studyId).map { ceTypes =>
          ceTypes.specimenDescriptions
        }
      (specimenDescriptions.size > 0).successNel[String]
    }
  }

  def processingTypeWithId(studyId: StudyId, processingTypeId: ProcessingTypeId)
      : ServiceValidation[ProcessingType] = {
    withStudy(studyId) { study =>
      processingTypeRepository.withId(study.id, processingTypeId)
    }
  }

  def processingTypesForStudy(studyId: StudyId)
      : ServiceValidation[Set[ProcessingType]] = {
    withStudy(studyId) { study =>
      processingTypeRepository.allForStudy(study.id).successNel[String]
    }
  }

  def specimenLinkTypeWithId(processingTypeId: ProcessingTypeId,
                             specimenLinkTypeId: SpecimenLinkTypeId)
      : ServiceValidation[SpecimenLinkType] = {
    validProcessingTypeId(processingTypeId) { processingType =>
      specimenLinkTypeRepository.withId(processingType.id, specimenLinkTypeId)
    }
  }

  def specimenLinkTypesForProcessingType(processingTypeId: ProcessingTypeId)
      : ServiceValidation[Set[SpecimenLinkType]] = {
    validProcessingTypeId(processingTypeId) { processingType =>
      specimenLinkTypeRepository.allForProcessingType(processingType.id).successNel[String]
    }
  }

  // def getProcessingDto(studyId: StudyId): ServiceValidation[ProcessingDto] = {
  //   "deprectated: annot type refactor".failureNel[ProcessingDto]
  // }

  private def withStudy[T](studyId: StudyId)(fn: Study => ServiceValidation[T])
      : ServiceValidation[T] = {
    for {
      study  <- studyRepository.getByKey(studyId)
      result <- fn(study)
    } yield result
  }

  private def validProcessingTypeId[T](processingTypeId: ProcessingTypeId)
                                   (fn: ProcessingType => ServiceValidation[T])
      : ServiceValidation[T] = {
    for {
      pt     <- processingTypeRepository.getByKey(processingTypeId)
      study  <- studyRepository.getByKey(pt.studyId)
      result <- fn(pt)
    } yield result
  }

  def processCommand(cmd: StudyCommand): Future[ServiceValidation[Study]] = {
    cmd.sessionUserId match {
      case None => Future.successful(Unauthorized.failureNel[Study])
      case Some(sessionUserId) =>
        val (permissionId, studyId) = cmd match {
            case c: StudyStateChangeCommand => (PermissionId.StudyChangeState, Some(StudyId(c.id)))
            case c: StudyModifyCommand      => (PermissionId.StudyUpdate, Some(StudyId(c.id)))
            case c: AddStudyCmd             => (PermissionId.StudyCreate, None)
          }

        whenPermittedAndIsMemberAsync(UserId(sessionUserId), permissionId, studyId, None) { () =>
          ask(processor, cmd).mapTo[ServiceValidation[StudyEvent]].map { validation =>
            for {
              event <- validation
              study <- studyRepository.getByKey(StudyId(event.id))
            } yield study
          }
        }
    }
  }

  def processProcessingTypeCommand(cmd: StudyCommand)
      : Future[ServiceValidation[ProcessingType]] = {
    cmd match {
      case c: RemoveProcessingTypeCmd =>
        Future.successful(ServiceError(s"invalid service call: $cmd").failureNel[ProcessingType])
      case _ =>
        ask(processor, cmd).mapTo[ServiceValidation[ProcessingTypeEvent]].map { validation =>
          for {
            event  <- validation
            result <- processingTypeRepository.getByKey(ProcessingTypeId(event.id))
          } yield result
        }
    }
  }

  def processRemoveProcessingTypeCommand(cmd: StudyCommand)
      : Future[ServiceValidation[Boolean]] =
    ask(processor, cmd).mapTo[ServiceValidation[ProcessingTypeEvent]]
      .map { validation => validation.map(_ => true) }

  private def filterStudiesInternal(unfilteredStudies: Set[Study], filter: FilterString, sort: SortString):
      ServiceValidation[Seq[Study]] = {
    val sortStr = if (sort.expression.isEmpty) new SortString("name")
                  else sort

    for {
      studies <- StudyFilter.filterStudies(unfilteredStudies, filter)
      sortExpressions <- {
        QuerySortParser(sortStr).toSuccessNel(ServiceError(s"could not parse sort expression: $sort"))
      }
      sortFunc <- {
        Study.sort2Compare.get(sortExpressions(0).name)
          .toSuccessNel(ServiceError(s"invalid sort field: ${sortExpressions(0).name}"))
      }
    } yield {
      val result = studies.toSeq.sortWith(sortFunc)
      if (sortExpressions(0).order == AscendingOrder) result
      else result.reverse
    }
  }

}
