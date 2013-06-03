package domain.study

import domain.Identity

case class SpecimenTypeId(identity: String) extends { val id = identity } with Identity {

}

object SpecimenGroupIdentityService {

  // TODO: not sure yet if this is the right place for this method
  def nextIdentity: SpecimenGroupId =
    new SpecimenGroupId(java.util.UUID.randomUUID.toString.toUpperCase)

}