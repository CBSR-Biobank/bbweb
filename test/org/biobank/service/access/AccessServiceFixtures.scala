package org.biobank.service.access

import org.biobank.domain.{ConcurrencySafeEntity, Factory}
import org.biobank.domain.access._
import org.biobank.domain.centre.{Centre, CentreRepository}
import org.biobank.domain.study.{Study, StudyRepository}
import org.biobank.domain.user._
import org.scalatest.Assertions

trait AccessServiceFixtures extends Assertions {

  case class MembershipFixtureParam(user: User, study: Study, centre: Centre) extends {
    val membership = factory.createMembership.copy(userIds    = Set(user.id),
                                                   studyData  = MembershipEntityData(false, Set(study.id)),
                                                   centreData = MembershipEntityData(false, Set(centre.id)))
  }

  val factory: Factory

  val accessItemRepository: AccessItemRepository

  val membershipRepository: MembershipRepository

  val userRepository: UserRepository

  val studyRepository: StudyRepository

  val centreRepository: CentreRepository

  protected def membershipFixture() = {
    val f = MembershipFixtureParam(user   = factory.createActiveUser,
                                   study  = factory.createEnabledStudy,
                                   centre = factory.createEnabledCentre)
    Set(f.user, f.study, f.centre, f.membership).foreach(addToRepository)
    f
  }

  protected def addToRepository[T <: ConcurrencySafeEntity[_]](entity: T): Unit = {
    entity match {
      case u: User       => userRepository.put(u)
      case i: AccessItem => accessItemRepository.put(i)
      case s: Study      => studyRepository.put(s)
      case c: Centre     => centreRepository.put(c)
      case m: Membership => membershipRepository.put(m)
      case _             => fail("invalid entity")
    }
  }

}
