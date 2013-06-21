package domain.study

import domain.IdentifiedValueObject

case class CollectionEventTypeId(identity: String) extends { val id = identity } with IdentifiedValueObject[String] {

}
