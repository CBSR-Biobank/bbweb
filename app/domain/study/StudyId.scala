package domain.study

import domain.Identity

case class StudyId(identity: String) extends { val id = identity } with Identity {
}

object StudyIdentityService {

  def nextIdentity: StudyId =
    new StudyId(java.util.UUID.randomUUID.toString.toUpperCase)

}