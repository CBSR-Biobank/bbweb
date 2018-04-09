package org.biobank.controllers.participants

import javax.inject.{Inject, Singleton}
import org.biobank.controllers._
import org.biobank.domain.Slug
import org.biobank.domain.participants.{CollectionEventId, SpecimenId}
import org.biobank.services._
import org.biobank.services.participants.SpecimensService
import play.api.libs.json._
import play.api.{ Environment, Logger }
import play.api.mvc.{Action, ControllerComponents}
import scala.concurrent.{ExecutionContext, Future}
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

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
      val v = service.getBySlug(request.authInfo.userId, slug)
        .flatMap { specimen => service.specimenToDto(specimen) }
      validationReply(v)
    }

  def list(ceventSlug: Slug): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      validationReply(
        Future {
          for {
            pagedQuery   <- PagedQuery.create(request.rawQueryString, PageSizeMax)
            specimens    <- service.listBySlug(request.authInfo.userId, ceventSlug, pagedQuery.sort)
            validPage    <- pagedQuery.validPage(specimens.size)
            specimenDtos <- specimens.map(s => service.specimenToDto(s)).toList.sequenceU
            results      <- PagedResults.create(specimenDtos, pagedQuery.page, pagedQuery.limit)
          } yield results
        })
    }

  def snapshot: Action[Unit] =
    action(parse.empty) { implicit request =>
      validationReply(service.snapshotRequest(request.authInfo.userId).map(_ => true))
    }

  def addSpecimens(ceventId: CollectionEventId): Action[JsValue] =
    commandAction[AddSpecimensCmd](Json.obj("collectionEventId" -> ceventId)) { cmd =>
      val future = service.processCommand(cmd)
      validationReply(future)
    }

  def removeSpecimen(ceventId: CollectionEventId, spcId: SpecimenId, ver: Long): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      val cmd = RemoveSpecimenCmd(
          sessionUserId         = request.authInfo.userId.id,
          id                    = spcId.id,
          collectionEventId     = ceventId.id,
          expectedVersion       = ver)
      val future = service.processRemoveCommand(cmd)
      validationReply(future)
    }

}
