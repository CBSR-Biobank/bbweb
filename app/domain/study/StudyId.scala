package domain.study

import domain.IdentifiedValueObject

case class StudyId(identity: String) extends { val id = identity } with IdentifiedValueObject[String] {
}

object StudyIdentityService {

  def nextIdentity: StudyId =
    new StudyId(java.util.UUID.randomUUID.toString.toUpperCase)

}