package domain.study

import domain.Identity

case class SpecimenGroupId(identity: String) extends { val id = identity } with Identity {

}