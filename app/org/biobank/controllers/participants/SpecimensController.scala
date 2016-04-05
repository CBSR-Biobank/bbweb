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

  var sortFunc = Map("id"          -> Specimen.compareById _,
                     "timeCreated" -> Specimen.compareByTimeCreated _,
                     "status"      -> Specimen.compareByStatus _)

  def list(ceventId: String,
           status:   String,
           sort:     String,
           page:     Int,
           pageSize: Int,
           order:    String) =
    AuthAction(parse.empty) { (token, userId, request) =>

      Logger.debug(s"SpecimensController:list: ceventId/$ceventId, sort/$sort, page/$page, pageSize/$pageSize, order/$order")

      val pagedQuery = PagedQuery(sort, page, pageSize, order)
      val validation = for {
        sortField   <- pagedQuery.getSortField(sortFunc.keys.toSeq)
        sortWith    <- sortFunc.get(sortField).toSuccessNel("invalid sort field")
        sortOrder   <- pagedQuery.getSortOrder
        specimens   <- service.list(ceventId, sortWith, sortOrder)
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
      val future = service.processAddCommand(cmd)
      domainValidationReply(future)
    }

}
