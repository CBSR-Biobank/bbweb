package org.biobank.domain.access

import com.google.inject.ImplementedBy
import javax.inject.{Inject, Singleton}
import org.biobank.{Global, TestData}
import org.biobank.domain._
import org.biobank.domain.centres.CentreId
import org.biobank.domain.studies.StudyId
import org.biobank.domain.users.UserId
//import org.slf4j.{Logger, LoggerFactory}
import scalaz.Scalaz._

@ImplementedBy(classOf[MembershipRepositoryImpl])
trait MembershipRepository extends ReadWriteRepositoryWithSlug[MembershipId, Membership] {

  def getUserMembership(userId: UserId): DomainValidation[UserMembership]

}

@Singleton
class MembershipRepositoryImpl @Inject() (val testData: TestData)
    extends ReadWriteRepositoryRefImplWithSlug[MembershipId, Membership](v => v.id)
    with MembershipRepository {

  import org.biobank.CommonValidations._

  //private val log: Logger = LoggerFactory.getLogger(this.getClass)

  override def init(): Unit = {
    super.init()
    val name = "All studies and all centres"
    put(Membership(id           = MembershipId(Slug(name).id),
                   version      = 0L,
                   timeAdded    = Global.StartOfTime,
                   timeModified = None,
                   slug         = Slug(name),
                   name         = name,
                   description  = None,
                   userIds      = Set(Global.DefaultUserId),
                   studyData    = MembershipEntitySet(true, Set.empty[StudyId]),
                   centreData   = MembershipEntitySet(true, Set.empty[CentreId])))

    testData.testMemberships.foreach(put)
  }

  def nextIdentity: MembershipId = new MembershipId(nextIdentityAsString)

  protected def notFound(id: MembershipId): IdNotFound = IdNotFound(s"membership id: $id")

  protected def slugNotFound(slug: Slug): EntityCriteriaNotFound =
    EntityCriteriaNotFound(s"membership slug: $slug")

  def getUserMembership(userId: UserId): DomainValidation[UserMembership] = {
    getValues
      .find { m => m.userIds.exists(_ == userId) }
      .map { m => UserMembership.create(m, userId) }
      .toSuccessNel(s"membership for user not found: $userId")
  }
}
