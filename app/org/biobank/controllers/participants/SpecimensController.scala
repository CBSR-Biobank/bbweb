package org.biobank.controllers.participants

import scala.language.reflectiveCalls
import javax.inject.{Inject, Singleton}
import org.biobank.controllers._
import org.biobank.domain.participants.Specimen
import org.biobank.service.AuthToken
import org.biobank.service.participants.SpecimensService
import org.biobank.service.users.UsersService
import play.api.libs.json._
import play.api.{ Environment, Logger }
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@Singleton
class SpecimensController @Inject() (val env:          Environment,
                                     val authToken:    AuthToken,
                                     val usersService: UsersService,
                                     val service:      SpecimensService)
    extends CommandController
    with JsonController {
  import org.biobank.infrastructure.command.SpecimenCommands._

  private val PageSizeMax = 10

  val listSortFields = Map[String, (Specimen, Specimen) => Boolean](
      "inventoryId" -> Specimen.compareByInventoryId,
      "timeCreated" -> Specimen.compareByTimeCreated,
      "status"      -> Specimen.compareByStatus)

  /**
   * Returns the specimen with the given ID.
   */
  def get(id: String) =
    AuthAction(parse.empty) { (token, userId, request) =>
      domainValidationReply(service.get(id))
    }

  def getByInventoryId(invId: String) =
    AuthAction(parse.empty) { (token, userId, request) =>
      domainValidationReply(service.getByInventoryId(invId))
    }

  def list(ceventId: String,
           status:   String,
           sort:     String,
           page:     Int,
           pageSize: Int,
           order:    String) =
    AuthAction(parse.empty) { (token, userId, request) =>

      Logger.debug(s"""|SpecimensController:list: ceventId/$ceventId, sort/$sort,
                       |  page/$page, pageSize/$pageSize, order/$order""".stripMargin)

      val pagedQuery = PagedQuery(listSortFields, page, pageSize, order)

      val validation = for {
          sortFunc    <- pagedQuery.getSortFunc(sort)
          sortOrder   <- pagedQuery.getSortOrder
          specimens   <- service.list(ceventId, sortFunc, sortOrder)
          page        <- pagedQuery.getPage(PageSizeMax, specimens.size)
          pageSize    <- pagedQuery.getPageSize(PageSizeMax)
          results     <- PagedResults.create(specimens, page, pageSize)
        } yield results

      validation.fold(
        err     => BadRequest(err.list.toList.mkString),
        results => Ok(results)
      )
    }

  def addSpecimens(ceventId: String) =
    commandAction(Json.obj("collectionEventId" -> ceventId)) { cmd: AddSpecimensCmd =>
      val future = service.processCommand(cmd)
      domainValidationReply(future)
    }

  def removeSpecimen(ceventId: String, spcId: String, ver: Long) =
    AuthActionAsync(parse.empty) { (token, userId, request) =>
      val cmd = RemoveSpecimenCmd(
          userId                = userId.id,
          id                    = spcId,
          collectionEventId     = ceventId,
          expectedVersion       = ver)
      val future = service.processRemoveCommand(cmd)
      domainValidationReply(future)
    }

}
