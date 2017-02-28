package org.biobank.controllers.participants

import javax.inject.{Inject, Singleton}
import org.biobank.controllers._
import org.biobank.domain.participants.{CollectionEventId, SpecimenId}
import org.biobank.service._
import org.biobank.service.participants.SpecimensService
import org.biobank.service.users.UsersService
import play.api.libs.json._
import play.api.{ Environment, Logger }
import play.api.mvc.Action
import scala.concurrent.{ExecutionContext, Future}
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
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

  val log: Logger = Logger(this.getClass)

  private val PageSizeMax = 10

  /**
   * Returns the specimen with the given ID.
   */
  def get(id: SpecimenId): Action[Unit] =
    action(parse.empty) { implicit request =>
      validationReply(service.get(id))
    }

  def getByInventoryId(invId: String): Action[Unit] =
    action(parse.empty) { implicit request =>
      validationReply(service.getByInventoryId(invId))
    }

  def list(ceventId: CollectionEventId): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      Future {
        val validation = for {
            pagedQuery <- PagedQuery.create(request.rawQueryString, PageSizeMax)
            specimens  <- service.list(ceventId, pagedQuery.sort)
            validPage  <- pagedQuery.validPage(specimens.size)
            results    <- PagedResults.create(specimens, pagedQuery.page, pagedQuery.limit)
          } yield results

        validation.fold(
          err     => BadRequest(err.list.toList.mkString),
          results => Ok(results)
        )
      }
    }

  def addSpecimens(ceventId: CollectionEventId): Action[JsValue] =
    commandActionAsync(Json.obj("collectionEventId" -> ceventId)) { cmd: AddSpecimensCmd =>
      val future = service.processCommand(cmd)
      validationReply(future)
    }

  def removeSpecimen(ceventId: CollectionEventId, spcId: SpecimenId, ver: Long): Action[Unit] =
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
