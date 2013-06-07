package domain.study

import domain.IdentifiedValueObject

case class CollectionEventId(identity: String) extends { val id = identity } with IdentifiedValueObject[String] {

}
