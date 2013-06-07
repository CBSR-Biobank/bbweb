package domain.study

import domain.IdentifiedValueObject

class CollectionEventAnnotationTypeId(identity: String) extends { val id = identity } with IdentifiedValueObject[String] {

}
