package org.biobank.services.centres

import akka.actor._
import akka.pattern.ask
import com.google.inject.ImplementedBy
import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Named}
import org.biobank.domain.{LocationId, Slug}
import org.biobank.domain.access._
import org.biobank.domain.access.PermissionId
import org.biobank.domain.centres._
import org.biobank.domain.users.UserId
import org.biobank.dto._
import org.biobank.infrastructure._
import org.biobank.infrastructure.commands.CentreCommands._
import org.biobank.infrastructure.events.CentreEvents._
import org.biobank.services._
import org.biobank.services.access.AccessService
import org.biobank.services.studies.StudiesService
import org.slf4j.{Logger, LoggerFactory}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@ImplementedBy(classOf[CentresServiceImpl])
trait CentresService extends BbwebService {

  /**
   * Returns a set of studies. The entities can be filtered and or sorted using expressions.
   *
   * @param filter the string representation of the filter expression to use to filter the studies.
   *
   * @param sort the string representation of the sort expression to use when sorting the studies.
   */
  def getCentresCount(requestUserId: UserId): ServiceValidation[Long]

  def getCountsByStatus(requestUserId: UserId): ServiceValidation[CentreCountsByStatus]

  def searchLocations(cmd: SearchCentreLocationsCmd): ServiceValidation[Set[CentreLocationInfo]]

  def getCentres(requestUserId: UserId, pagedQuery: PagedQuery)
      : Future[ServiceValidation[PagedResults[CentreDto]]]

  def getCentreNames(requestUserId: UserId,
                     filter:        FilterString,
                     sort:          SortString): Future[ServiceValidation[Seq[NameAndStateDto]]]

  def getCentre(requestUserId: UserId, id: CentreId): ServiceValidation[Centre]

  def getCentreBySlug(requestUserId: UserId, slug: Slug): ServiceValidation[CentreDto]

  def centreFromLocation(requestUserId: UserId, id: LocationId): ServiceValidation[Centre]

  def processCommand(cmd: CentreCommand): Future[ServiceValidation[CentreDto]]

  def snapshotRequest(requestUserId: UserId): ServiceValidation[Unit]

}

/**
 * This is the Centre Aggregate Application Service.
 *
 * Handles the commands to configure centres. the commands are forwarded to the Centre Aggregate
 * Processor.
 *
 * @param centreProcessor
 *
 */
class CentresServiceImpl @Inject() (@Named("centresProcessor") val processor: ActorRef,
                                    val accessService:             AccessService,
                                    val studiesService:            StudiesService,
                                    val centreRepository:          CentreRepository)
    extends CentresService
    with AccessChecksSerivce
    with CentreServicePermissionChecks {

  import org.biobank.CommonValidations._
  import org.biobank.domain.access.AccessItem._

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  def getCentresCount(requestUserId: UserId): ServiceValidation[Long] = {
    withPermittedCentres(requestUserId) { centres =>
      centres.size.toLong.successNel[String]
    }
  }

  def getCountsByStatus(requestUserId: UserId): ServiceValidation[CentreCountsByStatus] = {
    withPermittedCentres(requestUserId) { centres =>
      CentreCountsByStatus(
        total         = centres.size.toLong,
        disabledCount = centres.collect { case s: DisabledCentre => s }.size.toLong,
        enabledCount  = centres.collect { case s: EnabledCentre => s }.size.toLong
      ).successNel[String]
    }
  }

  def getCentres(requestUserId: UserId, query: PagedQuery)
      : Future[ServiceValidation[PagedResults[CentreDto]]] =  {
    Future {
      withPermittedCentres(requestUserId) { centres =>
        for {
          centres   <- filterCentresInternal(centres, query.filter, query.sort)
          validPage <- query.validPage(centres.size)
          dtos      <- centres.map(centreToDto(requestUserId, _)).toList.sequenceU.map(_.toSeq)
          result    <- PagedResults.create(dtos, query.page, query.limit)
        } yield result
      }
    }
  }

   def getCentreNames(requestUserId: UserId,
                      filter:        FilterString,
                      sort:          SortString): Future[ServiceValidation[Seq[NameAndStateDto]]] =  {
     Future {
       withPermittedCentres(requestUserId) { centres =>
         filterCentresInternal(centres, filter, sort).map { centres =>
           centres.map(c => NameAndStateDto(c.id.id, c.slug, c.name, c.state.id))
         }
       }
     }
   }

 def getCentre(requestUserId: UserId, id: CentreId): ServiceValidation[Centre] = {
    whenPermittedAndIsMember(requestUserId,
                             PermissionId.CentreRead,
                             None,
                             Some(id)) { () =>
      centreRepository.getByKey(id)
    }
  }

  def getCentreBySlug(requestUserId: UserId, slug: Slug): ServiceValidation[CentreDto] = {
    for {
      centre     <- centreRepository.getBySlug(slug)
      permission <- accessService.hasPermissionAndIsMember(requestUserId,
                                                           PermissionId.CentreRead,
                                                           None,
                                                           Some(centre.id))
      dto        <- centreToDto(requestUserId, centre)
      result     <- if (permission) dto.successNel[String] else Unauthorized.failureNel[CentreDto]
    } yield result
  }

  def centreFromLocation(requestUserId: UserId, id: LocationId): ServiceValidation[Centre] = {
    for {
      centre <- centreRepository.getByLocationId(id)
      permission <- accessService.hasPermissionAndIsMember(requestUserId,
                                                           PermissionId.CentreRead,
                                                           None,
                                                           Some(centre.id))
      authorized <- {
        if (permission) centre.successNel[String]
        else Unauthorized.failureNel[Centre]
      }
    } yield centre
  }

  def searchLocations(cmd: SearchCentreLocationsCmd): ServiceValidation[Set[CentreLocationInfo]] =  {
    withPermittedCentres(UserId(cmd.sessionUserId)) { centres =>
      val allLocationInfos = centres.flatMap { centre =>
          centre.locations.map { location =>
            CentreLocationInfo(centre.id.id, location.id.id, centre.name, location.name)
          }
        }

      val filterLowerCase = cmd.filter.toLowerCase.trim
      val filteredLocationInfos =
        if (filterLowerCase.isEmpty) {
          allLocationInfos
        } else {
          allLocationInfos.filter { l =>
            l.name.toLowerCase contains filterLowerCase
          }
        }

      filteredLocationInfos
        .toSeq
        .sortWith { (a, b) => (a.name compareToIgnoreCase b.name) < 0 }
        .take(cmd.limit)
        .toSet
        .successNel[String]
    }
  }

  def processCommand(cmd: CentreCommand): Future[ServiceValidation[CentreDto]] = {
    val (permissionId, centreId) = cmd match {
        case c: CentreStateChangeCommand => (PermissionId.CentreChangeState, Some(CentreId(c.id)))
        case c: CentreModifyCommand      => (PermissionId.CentreUpdate, Some(CentreId(c.id)))
        case c: AddCentreCmd             => (PermissionId.CentreCreate, None)
      }

    val requestUserId = UserId(cmd.sessionUserId)

    whenPermittedAndIsMemberAsync(requestUserId, permissionId, None, centreId) { () =>
      ask(processor, cmd).mapTo[ServiceValidation[CentreEvent]].map { validation =>
        for {
          event  <- validation
          centre <- centreRepository.getByKey(CentreId(event.id))
          dto    <- centreToDto(requestUserId, centre)
        } yield dto
      }
    }
  }

  def filterCentresInternal(unfilteredCentres: Set[Centre],
                            filter:            FilterString,
                            sort:              SortString): ServiceValidation[Seq[Centre]] =  {
    val sortStr = if (sort.expression.isEmpty) new SortString("name")
                  else sort

    for {
      centres <- CentreFilter.filterCentres(unfilteredCentres, filter)
      sortExpressions <- {
        QuerySortParser(sortStr).toSuccessNel(ServiceError(s"could not parse sort expression: $sort"))
      }
      sortFunc <- {
        Centre.sort2Compare.get(sortExpressions(0).name).
          toSuccessNel(ServiceError(s"invalid sort field: ${sortExpressions(0).name}"))
      }
    } yield {
      val result = centres.toSeq.sortWith(sortFunc)
      if (sortExpressions(0).order == AscendingOrder) result
      else result.reverse
    }
  }

  private def centreToDto(requestUserId: UserId, centre: Centre): ServiceValidation[CentreDto] = {
    val v = centre.studyIds
      .map { id =>
        studiesService.getStudy(requestUserId, id).map { study =>
          NameAndStateDto(study.id.id, study.slug, study.name, study.state.id)
        }
      }
      .toList.sequenceU

    v.map { studyNames =>
      CentreDto(id           = centre.id.id,
                version      = centre.version,
                timeAdded    = centre.timeAdded.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                timeModified = centre.timeModified.map(_.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)),
                state        = centre.state.id,
                slug         = centre.slug,
                name         = centre.name,
                description  = centre.description,
                studyNames   = studyNames.toSet,
                locations    = centre.locations)
    }
  }

}
