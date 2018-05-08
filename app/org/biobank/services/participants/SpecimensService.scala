package org.biobank.services.participants

import akka.actor._
import akka.pattern.ask
import com.google.inject.ImplementedBy
import javax.inject.{Inject, Named}
import org.biobank.domain.Slug
import org.biobank.domain.access._
import org.biobank.domain.centres.CentreRepository
import org.biobank.domain.participants._
import org.biobank.domain.studies._
import org.biobank.domain.users.UserId
import org.biobank.dto.SpecimenDto
import org.biobank.infrastructure.AscendingOrder
import org.biobank.infrastructure.commands.SpecimenCommands._
import org.biobank.infrastructure.events.SpecimenEvents._
import org.biobank.services._
import org.biobank.services.access.AccessService
import org.biobank.services.centres.CentreLocationInfo
import org.slf4j.{Logger, LoggerFactory}
import scala.concurrent._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@ImplementedBy(classOf[SpecimensServiceImpl])
trait SpecimensService extends BbwebService {

  def get(requestUserId: UserId, id: SpecimenId): ServiceValidation[SpecimenDto]

  def getBySlug(requestUserId: UserId, slug: Slug): ServiceValidation[SpecimenDto]

  def getByInventoryId(requestUserId: UserId, inventoryId: String): ServiceValidation[Specimen]

  def list(requestUserId: UserId, collectionEventId: CollectionEventId, query: PagedQuery)
      : Future[ServiceValidation[PagedResults[SpecimenDto]]]

  def listBySlug(requestUserId: UserId, eventSlug: Slug, query: PagedQuery)
      : Future[ServiceValidation[PagedResults[SpecimenDto]]]

  def processCommand(cmd: SpecimenCommand): Future[ServiceValidation[CollectionEvent]]

  def processRemoveCommand(cmd: SpecimenCommand): Future[ServiceValidation[Boolean]]


  def snapshotRequest(requestUserId: UserId): ServiceValidation[Unit]

}

@SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
class SpecimensServiceImpl @Inject() (
  @Named("specimensProcessor") val processor: ActorRef,
  val accessService:                          AccessService,
  val studyRepository:                        StudyRepository,
  val collectionEventRepository:              CollectionEventRepository,
  val collectionEventTypeRepository:          CollectionEventTypeRepository,
  val ceventSpecimenRepository:               CeventSpecimenRepository,
  val specimenRepository:                     SpecimenRepository,
  val centreRepository:                       CentreRepository)
                                  (implicit executionContext: BbwebExecutionContext)
    extends SpecimensService
    with AccessChecksSerivce
    with ServicePermissionChecks {

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  def get(requestUserId: UserId, id: SpecimenId): ServiceValidation[SpecimenDto] = {
    for {
      ceventSpecimen <- ceventSpecimenRepository.withSpecimenId(id)
      permitted      <- whenSpecimenPermitted(requestUserId,
                                              ceventSpecimen.ceventId)(_ => true.successNel[String])
      specimen       <- specimenRepository.getByKey(id)
      dto            <- specimenToDto(specimen)
    } yield dto
  }

  def getBySlug(requestUserId: UserId, slug: Slug): ServiceValidation[SpecimenDto] = {
    for {
      specimen       <- specimenRepository.getBySlug(slug)
      ceventSpecimen <- ceventSpecimenRepository.withSpecimenId(specimen.id)
      permitted      <- whenSpecimenPermitted(requestUserId,
                                              ceventSpecimen.ceventId)(_ => true.successNel[String])
      dto            <- specimenToDto(specimen)
    } yield dto
  }

  def getByInventoryId(requestUserId: UserId, inventoryId: String): ServiceValidation[Specimen] = {
    for {
      specimen       <- specimenRepository.getByInventoryId(inventoryId)
      ceventSpecimen <- ceventSpecimenRepository.withSpecimenId(specimen.id)
      permitted      <- {
        whenSpecimenPermitted(requestUserId, ceventSpecimen.ceventId)(_ => true.successNel[String])
      }
    } yield specimen
  }

  def list(requestUserId: UserId, collectionEventId: CollectionEventId, query: PagedQuery)
      : Future[ServiceValidation[PagedResults[SpecimenDto]]] = {
    Future {
      filterSpecimens(requestUserId, collectionEventId, query);
    }
  }

  def listBySlug(requestUserId: UserId, eventSlug: Slug, query: PagedQuery)
      : Future[ServiceValidation[PagedResults[SpecimenDto]]] = {
    Future {
      for {
        event     <- collectionEventRepository.getBySlug(eventSlug)
        specimens <- filterSpecimens(requestUserId, event.id, query)
      } yield specimens
    }
  }

  private def filterSpecimens(requestUserId: UserId, eventId: CollectionEventId, query: PagedQuery)
      : ServiceValidation[PagedResults[SpecimenDto]] = {
    for {
      permitted <- whenSpecimenPermitted(requestUserId, eventId)(_ => true.successNel[String])
      specimens <- sortSpecimens(eventId, query.sort)
      validPage <- query.validPage(specimens.size)
      dtos      <- specimens.map(specimenToDto).toList.sequenceU
      results   <- PagedResults.create(dtos, query.page, query.limit)
    } yield results
  }


  private def sortSpecimens(ceventId: CollectionEventId, sort: SortString)
      : ServiceValidation[List[Specimen]] = {
    val sortStr = if (sort.expression.isEmpty) new SortString("inventoryId")
                  else sort

    for {
      specimens <- {
        ceventSpecimenRepository.withCeventId(ceventId)
          .map { cs => specimenRepository.getByKey(cs.specimenId) }
          .toList
          .sequenceU
      }
      sortExpressions <- {
        QuerySortParser(sortStr).
          toSuccessNel(ServiceError(s"could not parse sort expression: ${sort}"))
      }
      sortFunc        <- {
        Specimen.sort2Compare.get(sortExpressions(0).name).
          toSuccessNel(ServiceError(s"invalid sort field: ${sortExpressions(0).name}"))
      }
    } yield {
      val result = specimens.sortWith(sortFunc)
      if (sortExpressions(0).order == AscendingOrder) result
      else result.reverse
    }
  }

  def processCommand(cmd: SpecimenCommand): Future[ServiceValidation[CollectionEvent]] = {
    val validCommand = cmd match {
        case c: RemoveSpecimenCmd =>
          ServiceError(s"invalid service call: $cmd, use processRemoveCommand").failureNel[CollectionEvent]
        case c => c.successNel[String]
      }

    validCommand.fold(
      err => Future.successful(err.failure[CollectionEvent]),
      _   => whenSpecimenPermittedAsync(cmd) { () =>
        ask(processor, cmd).mapTo[ServiceValidation[SpecimenEvent]].map { validation =>
          for {
            event  <- validation
            cevent <- collectionEventRepository.getByKey(CollectionEventId(event.getAdded.getCollectionEventId))
          } yield cevent
        }
      }
    )
  }

  def processRemoveCommand(cmd: SpecimenCommand): Future[ServiceValidation[Boolean]] = {
    whenSpecimenPermittedAsync(cmd) { () =>
      ask(processor, cmd).mapTo[ServiceValidation[SpecimenEvent]].map { validation =>
        for {
          event  <- validation
          result <- true.successNel[String]
        } yield result
      }
    }
  }

  //
  // Invokes function "block" if user that invoked this service has the permission and membership
  // to do so.
  //
  private def whenSpecimenPermitted[T](requestUserId: UserId, ceventId: CollectionEventId)
                                   (block: CollectionEvent => ServiceValidation[T]): ServiceValidation[T] = {
    for {
      cevent     <- collectionEventRepository.getByKey(ceventId)
      ceventType <- collectionEventTypeRepository.getByKey(cevent.collectionEventTypeId)
      study      <- studyRepository.getByKey(ceventType.studyId)
      result     <- whenPermittedAndIsMember(requestUserId,
                                             PermissionId.CollectionEventRead,
                                             Some(study.id),
                                             None)(() => block(cevent))
    } yield result
  }

  //
  // Invokes function "block" if user that issued the command has the permission and membership
  // to do so.
  //
  private def whenSpecimenPermittedAsync[T](cmd: SpecimenCommand)
                                        (block: () => Future[ServiceValidation[T]])
      : Future[ServiceValidation[T]] = {

    val validCeventId = cmd match {
        case c: SpecimenModifyCommand =>
          collectionEventRepository.getByKey(CollectionEventId(c.collectionEventId)).map(c => c.id)
        case c: SpecimenCommand => CollectionEventId(c.collectionEventId).successNel[String]
      }

    val permission = cmd match {
        case c: AddSpecimensCmd   => PermissionId.SpecimenCreate
        case c: RemoveSpecimenCmd => PermissionId.SpecimenDelete
        case c                    => PermissionId.SpecimenUpdate
      }

    val validStudy = for {
        ceventId   <- validCeventId
        cevent     <- collectionEventRepository.getByKey(ceventId)
        ceventType <- collectionEventTypeRepository.getByKey(cevent.collectionEventTypeId)
        study      <- studyRepository.getByKey(ceventType.studyId)
      } yield study

    validStudy.fold(
      err => Future.successful(err.failure[T]),
      study => whenPermittedAndIsMemberAsync(UserId(cmd.sessionUserId),
                                             permission,
                                             Some(study.id),
                                             None)(block)
    )
  }

  private def specimenToDto(specimen: Specimen): ServiceValidation[SpecimenDto] = {
    for {
      ceventSpecimen     <- ceventSpecimenRepository.withSpecimenId(specimen.id)
      cevent             <- collectionEventRepository.getByKey(ceventSpecimen.ceventId)
      ceventType         <- collectionEventTypeRepository.getByKey(cevent.collectionEventTypeId)
      specimenDesc       <- ceventType.specimenDesc(specimen.specimenDefinitionId)
      originCentre       <- centreRepository.getByLocationId(specimen.originLocationId)
      originLocationName <- originCentre.locationName(specimen.originLocationId)
      centre             <- centreRepository.getByLocationId(specimen.locationId)
      locationName       <- centre.locationName(specimen.locationId)
    } yield {
      val originLocationInfo = CentreLocationInfo(originCentre.id.id,
                                                  specimen.originLocationId.id,
                                                  originLocationName)
      val locationInfo = CentreLocationInfo(centre.id.id,
                                            specimen.locationId.id,
                                            locationName)
      specimen.createDto(cevent, ceventType.name, specimenDesc, originLocationInfo, locationInfo)
    }
  }

}
