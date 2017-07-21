package org.biobank.domain.access

import com.google.inject.ImplementedBy
import javax.inject.Singleton
import org.biobank.Global
import org.biobank.domain._
import org.biobank.domain.centre.CentreId
import org.biobank.domain.study.StudyId
import org.biobank.domain.user.UserId
//import org.slf4j.{Logger, LoggerFactory}
import scalaz.Scalaz._

@ImplementedBy(classOf[MembershipRepositoryImpl])
trait MembershipRepository extends ReadWriteRepository[MembershipId, Membership] {

  def getUserMembership(userId: UserId): DomainValidation[UserMembership]

}

@Singleton
class MembershipRepositoryImpl
    extends ReadWriteRepositoryRefImpl[MembershipId, Membership](v => v.id)
    with MembershipRepository {

  import org.biobank.CommonValidations._

  //private val log: Logger = LoggerFactory.getLogger(this.getClass)

  override def init(): Unit = {
    super.init()
    put(Membership(id           = nextIdentity,
                   version      = 0L,
                   timeAdded    = Global.StartOfTime,
                   timeModified = None,
                   name         = "All studies and all centres",
                   description  = None,
                   userIds      = Set(Global.DefaultUserId),
                   studyData    = MembershipEntityData(true, Set.empty[StudyId]),
                   centreData   = MembershipEntityData(true, Set.empty[CentreId])))
  }

  def nextIdentity: MembershipId = new MembershipId(nextIdentityAsString)

  def domainNotFound(id: MembershipId): IdNotFound = IdNotFound(s"membership id: $id")

  def getUserMembership(userId: UserId): DomainValidation[UserMembership] = {
    getValues
      .find { m => m.userIds.exists(_ == userId) }
      .map { m => UserMembership.create(m, userId) }
      .toSuccessNel(s"membership for user not found: $userId")
  }

  override def getByKey(id: MembershipId): DomainValidation[Membership] = {
    getMap.get(id).toSuccessNel(domainNotFound(id).toString)
  }
}
