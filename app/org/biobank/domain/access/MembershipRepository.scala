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

  def getUserMembership(userId: UserId): DomainValidation[Membership]

}

@Singleton
class MembershipRepositoryImpl
    extends ReadWriteRepositoryRefImpl[MembershipId, Membership](v => v.id)
    with MembershipRepository {

  import org.biobank.CommonValidations._

  //private val log: Logger = LoggerFactory.getLogger(this.getClass)

  def nextIdentity: MembershipId = new MembershipId(nextIdentityAsString)

  def domainNotFound(id: MembershipId): IdNotFound = IdNotFound(s"user id: $id")

  def getUserMembership(userId: UserId): DomainValidation[Membership] = {
    getValues.find { m => m.userIds.exists(_ == userId) }.toSuccessNel(s"membership for user not found: $userId")
  }

  override def getByKey(id: MembershipId): DomainValidation[Membership] = {
    getMap.get(id).toSuccessNel(domainNotFound(id).toString)
  }

  private def init(): Unit = {
    put(Membership(id           = nextIdentity,
                   version      = 0L,
                   timeAdded    = Global.StartOfTime,
                   timeModified = None,
                   userIds      = Set(Global.DefaultUserId),
                   studyInfo    = MembershipStudyInfo(true, Set.empty[StudyId]),
                   centreInfo   = MembershipCentreInfo(true, Set.empty[CentreId])))
  }

  init
}
