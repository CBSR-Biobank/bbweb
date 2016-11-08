package org.biobank.controllers.participants

import javax.inject.{Inject, Singleton}
import org.biobank.controllers._
import org.biobank.domain.participants.{CollectionEventId, Specimen, SpecimenId}
import org.biobank.service.{AuthToken, PagedQuery, PagedResults}
import org.biobank.service.participants.SpecimensService
import org.biobank.service.users.UsersService
import play.api.libs.json._
import play.api.{ Environment, Logger }
import scala.concurrent.{ExecutionContext, Future}
import scala.language.reflectiveCalls
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@Singleton
class SpecimensController @Inject() (val action:       BbwebAction,
                                     val env:          Environment,
                                     val authToken:    AuthToken,
                                     val usersService: UsersService,
                                     val service:      SpecimensService)
                                 (implicit ec: ExecutionContext)
    extends CommandController
    with JsonController {
  import org.biobank.infrastructure.command.SpecimenCommands._

  val log = Logger(this.getClass)

  private val PageSizeMax = 10

  /**
   * Returns the specimen with the given ID.
   */
  def get(id: SpecimenId) =
    action(parse.empty) { implicit request =>
      validationReply(service.get(id))
    }

  def getByInventoryId(invId: String) =
    action(parse.empty) { implicit request =>
      validationReply(service.getByInventoryId(invId))
    }

  def list(ceventId:      CollectionEventId,
           sortMaybe:     Option[String],
           pageMaybe:     Option[Int],
           limitMaybe: Option[Int],
           orderMaybe:    Option[String]) =
    action.async(parse.empty) { implicit request =>
      Future {
        val sort     = sortMaybe.fold { "inventoryId" } { s => s }
        val page     = pageMaybe.fold { 1 } { p => p }
        val limit = limitMaybe.fold { 5 } { ps => ps }
        val order    = orderMaybe.fold { "asc" } { o => o }

        log.debug(s"""|SpecimensController:list: ceventId/$ceventId, sort/$sort,
                      |  page/$page, limit/$limit, order/$order""".stripMargin)

        val pagedQuery = PagedQuery(page, limit, order)

        val validation = for {
            sortFunc    <- Specimen.sort2Compare.get(sort).toSuccessNel(ControllerError(s"invalid sort field: $sort"))
            sortOrder   <- pagedQuery.getSortOrder
            specimens   <- service.list(ceventId, sortFunc, sortOrder)
            page        <- pagedQuery.getPage(PageSizeMax, specimens.size)
            limit    <- pagedQuery.getPageSize(PageSizeMax)
            results     <- PagedResults.create(specimens, page, limit)
          } yield results

        validation.fold(
          err     => BadRequest(err.list.toList.mkString),
          results => Ok(results)
        )
      }
    }

  def addSpecimens(ceventId: CollectionEventId) =
    commandActionAsync(Json.obj("collectionEventId" -> ceventId)) { cmd: AddSpecimensCmd =>
      val future = service.processCommand(cmd)
      validationReply(future)
    }

  def removeSpecimen(ceventId: CollectionEventId, spcId: SpecimenId, ver: Long) =
    action.async(parse.empty) { implicit request =>
      val cmd = RemoveSpecimenCmd(
          userId                = request.authInfo.userId.id,
          id                    = spcId.id,
          collectionEventId     = ceventId.id,
          expectedVersion       = ver)
      val future = service.processRemoveCommand(cmd)
      validationReply(future)
    }

}
