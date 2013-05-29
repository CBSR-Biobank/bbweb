package domain.study

import domain.Identity

case class SpecimenTypeId(identity: String) extends { val id = identity } with Identity {

}