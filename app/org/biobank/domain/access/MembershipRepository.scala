package org.biobank.domain.access

import com.google.inject.ImplementedBy
import javax.inject.Singleton
import org.biobank.domain._
import org.biobank.domain.user.UserId
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

  // only existing users get a doamin, no need to generate a new ID
  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  def nextIdentity: MembershipId = throw new IllegalStateException("should not be used")

  def domainNotFound(id: MembershipId): IdNotFound = IdNotFound(s"user id: $id")

  def getUserMembership(userId: UserId): DomainValidation[Membership] = {
    getValues.find { m => m.userIds.exists(_ == userId) }.toSuccessNel(s"membership for user not found: $userId")
  }

  override def getByKey(id: MembershipId): DomainValidation[Membership] = {
    getMap.get(id).toSuccessNel(domainNotFound(id).toString)
  }

}
