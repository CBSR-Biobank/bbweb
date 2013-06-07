package domain.study

import domain.IdentifiedValueObject

case class SpecimenGroupId(identity: String) extends { val id = identity } with IdentifiedValueObject[String] {

}

object SpecimenGroupIdentityService {

  def nextIdentity: SpecimenGroupId =
    new SpecimenGroupId(java.util.UUID.randomUUID.toString.toUpperCase)

}