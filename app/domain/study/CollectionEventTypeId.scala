package domain.study

import domain.IdentifiedValueObject

case class CollectionEventTypeId(identity: String) extends { val id = identity } with IdentifiedValueObject[String] {

}

object CollectionEventTypeIdentityService {

  def nextIdentity: CollectionEventTypeId =
    new CollectionEventTypeId(java.util.UUID.randomUUID.toString.toUpperCase)

}