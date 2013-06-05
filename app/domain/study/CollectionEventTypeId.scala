package domain.study

import domain.Identity

case class CollectionEventTypeId(identity: String) extends { val id = identity } with Identity {

}

object CollectionEventTypeIdentityService {

  def nextIdentity: CollectionEventTypeId =
    new CollectionEventTypeId(java.util.UUID.randomUUID.toString.toUpperCase)

}