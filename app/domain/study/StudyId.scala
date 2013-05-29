package domain.study

import domain.Identity

case class StudyId(identity: String) extends { val id = identity } with Identity {
}