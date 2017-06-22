package org.biobank.controllers.access

import javax.inject.{Inject, Singleton}
import org.biobank.controllers._
import org.biobank.service.PagedResults
import org.biobank.service.studies.StudiesService
import org.biobank.service.access.AccessService
import play.api.Logger
//import play.api.libs.json.Reads._
//import play.api.libs.json._
import play.api.mvc._
import play.api.{Environment, Logger}
import scala.concurrent.{ExecutionContext, Future}
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._
import scalaz._

@SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
@Singleton
class AccessController @Inject() (val action:         BbwebAction,
                                  val env:            Environment,
                                  val accessService:   AccessService,
                                  val studiesService: StudiesService)
                              (implicit val ec: ExecutionContext)
    extends CommandController {

  val log: Logger = Logger(this.getClass)

  private val PageSizeMax = 20

  def listRoles: Action[Unit] =
    action.async(parse.empty) { implicit request =>
      validationReply(
        Future {
          for {
            pagedQuery <- PagedQuery.create(request.rawQueryString, PageSizeMax)
            access     <- accessService.getRoles(request.authInfo.userId, pagedQuery.filter, pagedQuery.sort)
            validPage  <- pagedQuery.validPage(access.size)
            results    <- PagedResults.create(access, pagedQuery.page, pagedQuery.limit)
          } yield results
        }
      )
    }

  // private def processCommand(cmd: UserCommand) = {
  //   validationReply(accessService.processCommand(cmd))
  // }
}
