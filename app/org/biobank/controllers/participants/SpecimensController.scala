package org.biobank.controllers.participants

import javax.inject.{Inject, Singleton}
import org.biobank.controllers._
import org.biobank.domain.Slug
import org.biobank.domain.participants.{CollectionEventId, SpecimenId}
import org.biobank.dto.SpecimenDto
import org.biobank.services._
import org.biobank.services.participants.SpecimensService
import play.api.libs.json._
import play.api.{ Environment, Logger }
import play.api.mvc.{Action, ControllerComponents}
import scala.concurrent.{ExecutionContext, Future}
import scalaz.Scalaz._

@SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
@Singleton
class SpecimensController @Inject() (controllerComponents: ControllerComponents,
                                     val action:       BbwebAction,
                                     val env:          Environment,
                                     val service:      SpecimensService)
                                 (implicit val ec: ExecutionContext)
    extends CommandController(controllerComponents) {
  import org.biobank.infrastructure.commands.SpecimenCommands._

  val log: Logger = Logger(this.getClass)

  private val PageSizeMax = 10

  /**
   * Returns the specimen with the given ID.
   */
  def get(slug: Slug): Action[Unit] =
    action(parse.empty) { implicit request =>
      val v = service.getBySlug(request.identity.user.id, slug)
      validationReply(v)
    }

  def list(ceventSlug: Slug): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      PagedQueryHelper(request.rawQueryString, PageSizeMax).fold(
        err => {
          validationReply(Future.successful(err.failure[PagedResults[SpecimenDto]]))
        },
        pagedQuery => {
          validationReply(service.listBySlug(request.identity.user.id, ceventSlug, pagedQuery))
        }
      )
    }

  def snapshot: Action[Unit] =
    action(parse.empty) { implicit request =>
      validationReply(service.snapshotRequest(request.identity.user.id).map(_ => true))
    }

  def addSpecimens(ceventId: CollectionEventId): Action[JsValue] =
    commandAction[AddSpecimensCmd](Json.obj("collectionEventId" -> ceventId)) { cmd =>
      val future = service.processCommand(cmd)
      validationReply(future)
    }

  def removeSpecimen(ceventId: CollectionEventId, spcId: SpecimenId, ver: Long): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      val cmd = RemoveSpecimenCmd(
          sessionUserId         = request.identity.user.id.id,
          id                    = spcId.id,
          collectionEventId     = ceventId.id,
          expectedVersion       = ver)
      val future = service.processRemoveCommand(cmd)
      validationReply(future)
    }

}
