package domain.study

import domain.IdentifiedValueObject

case class CollectionEventId(val id: String) extends IdentifiedValueObject[String] {}
