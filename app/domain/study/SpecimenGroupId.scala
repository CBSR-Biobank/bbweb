package domain.study

import domain.IdentifiedValueObject

case class SpecimenGroupId(identity: String) extends { val id = identity } with IdentifiedValueObject[String] {

}