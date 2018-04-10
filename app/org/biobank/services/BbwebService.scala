package org.biobank.services

import akka.actor.ActorRef
import akka.util.Timeout
import com.github.ghik.silencer.silent
import org.biobank.domain.access.PermissionId
import org.biobank.domain.access.PermissionId._
import org.biobank.domain.centres.CentreId
import org.biobank.domain.studies.StudyId
import org.biobank.domain.users.UserId
import org.biobank.services.access.AccessService
import scala.concurrent.Future
import scala.concurrent.duration._
import scalaz.Scalaz._

trait BbwebService

trait BbwebServiceImpl {

  implicit val timeout: Timeout = 5.seconds

  val processor: ActorRef

  @silent def snapshotRequest(requestUserId: UserId): ServiceValidation[Unit] = {
    (processor ! "snap").successNel[String]
  }

}

trait AccessChecksSerivce extends BbwebServiceImpl {

  protected val accessService: AccessService

  override def snapshotRequest(requestUserId: UserId): ServiceValidation[Unit] = {
    whenPermitted(requestUserId, PermissionId.Snapshot) { () =>
      super.snapshotRequest(requestUserId)
    }
  }

  protected def whenPermitted[T](requestUserId: UserId, permissionId: PermissionId)
                           (block: () => ServiceValidation[T]): ServiceValidation[T]
}


trait ServicePermissionChecks {

  import org.biobank.CommonValidations._
  import org.biobank.domain.access.AccessItem._
  import org.biobank.domain.access.PermissionId._

  protected val accessService: AccessService

  protected def whenPermitted[T](requestUserId: UserId, permissionId: PermissionId)
                           (block: () => ServiceValidation[T]): ServiceValidation[T] = {
    accessService.hasPermission(requestUserId, permissionId).fold(
      err        => err.failure[T],
      permission => if (permission) block()
                    else Unauthorized.failureNel[T]
    )
  }

  protected def whenPermittedAsync[T](requestUserId: UserId, permissionId: PermissionId)
                                  (block: () => Future[ServiceValidation[T]])
      : Future[ServiceValidation[T]] = {
    accessService.hasPermission(requestUserId, permissionId)
      .fold(
        err        => Future.successful(err.failure[T]),
        permission => if (permission) block()
                      else Future.successful(Unauthorized.failureNel[T])
      )
  }

  protected def whenPermittedAndIsMember[T](requestUserId: UserId,
                                            permissionId: PermissionId,
                                            studyId:      Option[StudyId],
                                            centreId:     Option[CentreId])
                                        (block: () => ServiceValidation[T]): ServiceValidation[T] = {
    accessService.hasPermissionAndIsMember(requestUserId, permissionId, studyId, centreId)
      .fold(
        err        => err.failure[T],
        permission => if (permission) block()
                      else Unauthorized.failureNel[T]
      )

  }

  protected def whenPermittedAndIsMemberAsync[T](requestUserId: UserId,
                                                 permissionId: PermissionId,
                                                 studyId:      Option[StudyId],
                                                 centreId:     Option[CentreId])
                                             (block: () => Future[ServiceValidation[T]])
      : Future[ServiceValidation[T]] = {
    accessService.hasPermissionAndIsMember(requestUserId, permissionId, studyId, centreId)
      .fold(
        err        => Future.successful(err.failure[T]),
        permission => if (permission) block()
                      else Future.successful(Unauthorized.failureNel[T])
      )
  }
}
