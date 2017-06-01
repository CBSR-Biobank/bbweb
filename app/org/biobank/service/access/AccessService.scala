package org.biobank.service.access

import akka.actor._
import akka.pattern.ask
import com.google.inject.ImplementedBy
import javax.inject._
import org.biobank.domain.access._
import org.biobank.domain.access.RoleId._
import org.biobank.domain.user.{UserId, UserRepository}
import org.biobank.domain.study.{StudyId, StudyRepository}
import org.biobank.domain.centre.{CentreId, CentreRepository}
import org.biobank.infrastructure.command.AccessCommands._
import org.biobank.infrastructure.event.AccessEvents._
import org.biobank.service._
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@ImplementedBy(classOf[AccessServiceImpl])
trait AccessService extends BbwebService {

  def assignRole(cmd: AddUserToRoleCmd): Future[ServiceValidation[Role]]

  def hasPermission(userId: UserId, permissionId: AccessItemId): ServiceValidation[Boolean]

  def isMember(userId:   UserId,
               studyId:  Option[StudyId],
               centreId: Option[CentreId]): ServiceValidation[Boolean]

  def hasPermissionAndIsMember(userId:       UserId,
                               permissionId: AccessItemId,
                               studyId:      Option[StudyId],
                               centreId:     Option[CentreId]): ServiceValidation[Boolean]

  def getRoles(userId: UserId): Set[RoleId]

  def getMembership(userId: UserId): ServiceValidation[Membership]

}

class AccessServiceImpl @Inject() (@Named("accessProcessor") val processor: ActorRef,
                                   val accessItemRepository:                AccessItemRepository,
                                   val membershipRepository:                MembershipRepository,
                                   val userRepository:                      UserRepository,
                                   val studyRepository:                     StudyRepository,
                                   val centreRepository:                    CentreRepository)
    extends AccessService
    with BbwebServiceImpl {

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  def assignRole(cmd: AddUserToRoleCmd): Future[ServiceValidation[Role]] = {
    for {
      user <- Future { userRepository.getByKey(UserId(cmd.userId)) }
      role <- processRoleCommand(cmd)
    } yield role
  }

  def hasPermission(userId: UserId, permissionId: AccessItemId): ServiceValidation[Boolean] = {
    val v = userRepository.getByKey(userId).flatMap { _ =>
        hasPermissionInternal(userId, permissionId)
      }
    log.debug(s"hasPermission: $v")
    v
  }

  def isMember(userId:   UserId,
               studyId:  Option[StudyId],
               centreId: Option[CentreId]): ServiceValidation[Boolean] = {
    (studyId, centreId) match {
      case (None, None) => false.successNel[String]
      case _ =>
        val validBoolean = true.successNel[String]
        for {
          user         <- userRepository.getByKey(userId)
          studyValid   <- studyId.map(studyRepository.getByKey(_).map(_ => true)).getOrElse(validBoolean)
          centreValid  <- centreId.map(centreRepository.getByKey(_).map(_ => true)).getOrElse(validBoolean)
          isMember     <- isMemberInternal(userId, studyId, centreId)
        } yield isMember
    }
  }

  def hasPermissionAndIsMember(userId:       UserId,
                               permissionId: AccessItemId,
                               studyId:      Option[StudyId],
                               centreId:     Option[CentreId]): ServiceValidation[Boolean] = {
    for {
      permission <- hasPermissionInternal(userId, permissionId)
      member     <- isMemberInternal(userId, studyId, centreId)
    } yield (permission && member)
  }

  def getRoles(userId: UserId): Set[RoleId] = {
    accessItemRepository.rolesForUser(userId).map(r => RoleId.withName(r.id.id))
  }

  def getMembership(userId: UserId): ServiceValidation[Membership] = {
    membershipRepository.getUserMembership(userId)
  }

  def processRoleCommand(cmd: AccessCommand): Future[ServiceValidation[Role]] = {
    log.debug(s"processRoleCommand: cmd: $cmd")
    ask(processor, cmd).mapTo[ServiceValidation[AccessEvent]].map { validation =>
      for {
        event <- validation
        role  <- accessItemRepository.getRole(AccessItemId(event.getRole.getRoleId))
      } yield role
    }
  }

  // should only called if userId is valid
  private def hasPermissionInternal(userId: UserId, permissionId: AccessItemId): ServiceValidation[Boolean] = {

    def hasPermissionParents(parentIds: Set[AccessItemId]): Boolean = {
      val found = parentIds.find { id =>
          accessItemRepository.getByKey(id).fold(
            err => false,
            item => checkItemAccess(item)
          )
        }.toSuccessNel("not allowed")
      log.debug(s"hasPermissionParents: ${found}")
      found.fold(err => false, _ => true)
    }

    def checkItemAccess(item: AccessItem): Boolean = {
      item match {
        case permission: Permission =>
          log.debug(s"checkItemAccess: ${item.id}, checking permission parents: ${permission.parentIds}")
          hasPermissionParents(permission.parentIds)

        case role: Role =>
          val result = if (role.userIds.exists(_ == userId)) {
              true
            } else {
              log.debug(s"checkItemAccess: ${item.id}, checking role parents")
              hasPermissionParents(role.parentIds)
            }
          log.debug(s"checkItemAccess: role: ${item.id}, result: $result")
          result
      }
    }

    log.debug(s"hasPermission: userId: $userId, permissionId: $permissionId")
    accessItemRepository.getByKey(permissionId).map(checkItemAccess)
  }

  // should only called if userId is valid, studyId is None or valid, and centreId is None or valid
  private def isMemberInternal(userId:   UserId,
                               studyId:  Option[StudyId],
                               centreId: Option[CentreId]): ServiceValidation[Boolean] = {

    val membership = membershipRepository
      .getUserMembership(userId).map { membership =>
        membership.isMember(studyId, centreId)
      }

    log.debug(s"isMemberInternal: userId: $userId, studyId: $studyId, centreId: $centreId, membership: $membership")
    membership
  }
}
