package org.biobank.controllers.participants

import javax.inject.{Inject, Singleton}
import org.biobank.controllers._
import org.biobank.domain.participants.CollectionEvent
import org.biobank.infrastructure.command.CollectionEventCommands._
import org.biobank.service.AuthToken
import org.biobank.service.participants.CollectionEventsService
import org.biobank.service.users.UsersService
import play.api.libs.json._
import play.api.{ Environment, Logger }
import scala.language.reflectiveCalls
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@Singleton
class CollectionEventsController @Inject() (val env:          Environment,
                                            val authToken:    AuthToken,
                                            val usersService: UsersService,
                                            val service:      CollectionEventsService)
    extends CommandController
    with JsonController {

  private val PageSizeMax = 10

  val listSortFields = Map[String, (CollectionEvent, CollectionEvent) => Boolean](
      "visitNumber"   -> CollectionEvent.compareByVisitNumber,
      "timeCompleted" -> CollectionEvent.compareByTimeCompleted)

  def get(ceventId: String) =
    AuthAction(parse.empty) { (token, userId, request) =>
      domainValidationReply(service.get(ceventId))
    }

  def list(participantId: String,
           sortMaybe:     Option[String],
           pageMaybe:     Option[Int],
           pageSizeMaybe: Option[Int],
           orderMaybe:    Option[String]) =
    AuthAction(parse.empty) { (token, userId, request) =>

      val sort     = sortMaybe.fold { "visitNumber" } { s => s }
      val page     = pageMaybe.fold { 1 } { p => p }
      val pageSize = pageSizeMaybe.fold { 5 } { ps => ps }
      val order    = orderMaybe.fold { "asc" } { o => o }

      Logger.debug(s"""|CollectionEventsController:list: participantId/$participantId,
                       |  sort/$sort, page/$page, pageSize/$pageSize, order/$order""".stripMargin)

      val pagedQuery = PagedQuery(listSortFields, page, pageSize, order)

      val validation = for {
          sortFunc    <- pagedQuery.getSortFunc(sort)
          sortOrder   <- pagedQuery.getSortOrder
          cevents     <- service.list(participantId, sortFunc, sortOrder)
          page        <- pagedQuery.getPage(PageSizeMax, cevents.size)
          pageSize    <- pagedQuery.getPageSize(PageSizeMax)
          results     <- PagedResults.create(cevents, page, pageSize)
        } yield results

      validation.fold(
        err     => BadRequest(err.list.toList.mkString),
        results =>  Ok(results)
      )
    }

  def getByVisitNumber(participantId: String, visitNumber: Int) =
    AuthAction(parse.empty) { (token, userId, request) =>
      domainValidationReply(service.getByVisitNumber(participantId, visitNumber))
    }

  def add(participantId: String) =
    commandAction(Json.obj("participantId" -> participantId)) { cmd: AddCollectionEventCmd =>
      processCommand(cmd)
    }

  def updateVisitNumber(ceventId: String) =
    commandAction(Json.obj("id" -> ceventId)) { cmd: UpdateCollectionEventVisitNumberCmd =>
      processCommand(cmd)
    }

  def updateTimeCompleted(ceventId: String) =
    commandAction(Json.obj("id" -> ceventId)) { cmd: UpdateCollectionEventTimeCompletedCmd =>
      processCommand(cmd)
    }

  def addAnnotation(ceventId: String) =
    commandAction(Json.obj("id" -> ceventId)) { cmd: UpdateCollectionEventAnnotationCmd =>
      processCommand(cmd)
    }

  def removeAnnotation(ceventId:      String,
                       annotTypeId:   String,
                       ver:           Long) =
    AuthActionAsync(parse.empty) { (token, userId, request) =>
      val cmd = RemoveCollectionEventAnnotationCmd(userId           = userId.id,
                                                   id               = ceventId,
                                                   expectedVersion  = ver,
                                                   annotationTypeId = annotTypeId)
      processCommand(cmd)
    }

  def addSpecimens(ceventId: String) =
    commandAction(Json.obj("id" -> ceventId)) { cmd: UpdateCollectionEventTimeCompletedCmd =>
      processCommand(cmd)
    }

  def remove(participantId: String, ceventId: String, ver: Long) =
    AuthActionAsync(parse.empty) { (token, userId, request) =>
      val cmd = RemoveCollectionEventCmd(userId          = userId.id,
                                         id              = ceventId,
                                         participantId   = participantId,
                                         expectedVersion = ver)
      val future = service.processRemoveCommand(cmd)
      domainValidationReply(future)
    }

  private def processCommand(cmd: CollectionEventCommand) = {
    val future = service.processCommand(cmd)
    domainValidationReply(future)
  }

}
