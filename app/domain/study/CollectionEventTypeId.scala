package domain.study

import domain.Identity

case class CollectionEventTypeId(identity: String) extends { val id = identity } with Identity {

}