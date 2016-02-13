package org.biobank.controllers.participants

import org.biobank.domain.participants.CollectionEvent
import org.biobank.controllers._
import org.biobank.service.AuthToken
import org.biobank.service.users.UsersService
import org.biobank.service.participants.ParticipantsService

import javax.inject.{Inject => javaxInject}
import play.api.Logger
import scala.concurrent.Future
import scala.language.reflectiveCalls
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

class CollectionEventsController @javaxInject() (val authToken:      AuthToken,
                                                 val usersService:   UsersService,
                                                 val participantsService: ParticipantsService)
    extends CommandController
    with JsonController {

  import org.biobank.infrastructure.command.ParticipantCommands._

  private val PageSizeMax = 10

  def get(participantId: String, ceventId: String) =
    AuthAction(parse.empty) { (token, userId, request) =>
      domainValidationReply(participantsService.getCollectionEvent(participantId, ceventId))
    }

  def list(participantId: String,
           sort:          String,
           page:          Int,
           pageSize:      Int,
           order:         String) =
    AuthAction(parse.empty) { (token, userId, request) =>

      Logger.debug(s"CollectionEventsController:list: participantId/participantId, sort/$sort, page/$page, pageSize/$pageSize, order/$order")

      val pagedQuery = PagedQuery(sort, page, pageSize, order)
      val validation = for {
        sortField   <- pagedQuery.getSortField(Seq("visitNumber", "timeCompleted"))
        sortWith    <- (if (sortField == "visitNumber") (CollectionEvent.compareByVisitNumber _)
                        else (CollectionEvent.compareByTimeCompleted _)).success
        sortOrder   <- pagedQuery.getSortOrder
        cevents     <- participantsService.getCollectionEvents(participantId, sortWith, sortOrder)
        page        <- pagedQuery.getPage(PageSizeMax, cevents.size)
        pageSize    <- pagedQuery.getPageSize(PageSizeMax)
        results     <- PagedResults.create(cevents, page, pageSize)
      } yield results

      validation.fold(
        err => BadRequest(err.list.toList.mkString),
        results =>  Ok(results)
      )
    }

  def getByVisitNumber(participantId: String, vn: Int) =
    AuthAction(parse.empty) { (token, userId, request) =>
      domainValidationReply(participantsService.getCollectionEventByVisitNumber(participantId, vn))
    }

  def addCollectionEvent() =
    commandAction { cmd: AddCollectionEventCmd =>
      val future = participantsService.addCollectionEvent(cmd)
      domainValidationReply(future)
    }

  def updateCollectionEvent() =
    commandAction { cmd: UpdateCollectionEventCmd =>
      val future = participantsService.updateCollectionEvent(cmd)
      domainValidationReply(future)
    }

  def removeCollectionEvent(participantId: String, id: String, ver: Long) =
    AuthActionAsync(parse.empty) { (token, userId, request) =>
      val cmd = RemoveCollectionEventCmd(userId          = Some(userId.id),
                                         id              = id,
                                         participantId   = participantId,
                                         expectedVersion = ver)
      val future = participantsService.removeCollectionEvent(cmd)
      domainValidationReply(future)
    }

}
