package org.biobank.service

import akka.actor.ActorRef
import akka.util.Timeout
import org.biobank.domain.access.PermissionId
import org.biobank.domain.centre.CentreId
import org.biobank.domain.study.StudyId
import org.biobank.domain.user.UserId
import org.biobank.service.access.AccessService
import scala.concurrent.Future
import scala.concurrent.duration._
import scalaz.Scalaz._

trait BbwebService

trait BbwebServiceImpl {

  implicit val timeout: Timeout = 5.seconds

  val processor: ActorRef

  def snapshotRequest(requestUserId: UserId): ServiceValidation[Unit] = {
    (processor ! "snap").successNel[String]
  }

}

trait ServiceWithPermissionChecks extends BbwebServiceImpl {

  import org.biobank.CommonValidations._
  import org.biobank.domain.access.AccessItem._
  import org.biobank.domain.access.PermissionId._

  val accessService: AccessService

  override def snapshotRequest(requestUserId: UserId): ServiceValidation[Unit] = {
    whenPermitted(requestUserId, PermissionId.Snapshot) { () =>
      super.snapshotRequest(requestUserId)
    }
  }

  protected def whenPermitted[T](requestUserId: UserId, permissionId: PermissionId)
                           (block: () => ServiceValidation[T]): ServiceValidation[T] = {
    if (accessService.hasPermission(requestUserId, permissionId).exists(permission => permission)) {
      block()
    } else {
      Unauthorized.failureNel[T]
    }
  }

  protected def whenPermittedAsync[T](requestUserId: UserId, permissionId: PermissionId)
                                  (block: () => Future[ServiceValidation[T]])
      : Future[ServiceValidation[T]] = {
    if (accessService.hasPermission(requestUserId, permissionId).exists(permission => permission)) {
      block()
    } else {
      Future.successful(Unauthorized.failureNel[T])
    }
  }

  protected def whenPermittedAndIsMemberAsync[T](requestUserId: UserId,
                                                 permissionId: PermissionId,
                                                 studyId:      Option[StudyId],
                                                 centreId:     Option[CentreId])
                                             (block: () => Future[ServiceValidation[T]])
      : Future[ServiceValidation[T]] = {
    val v = accessService.hasPermissionAndIsMember(requestUserId, permissionId, studyId, centreId)
    if (v.exists(permission => permission)) {
      block()
    } else {
      Future.successful(Unauthorized.failureNel[T])
    }
  }
}
