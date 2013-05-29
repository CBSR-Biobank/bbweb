package domain.study

import domain.Identity

case class CollectionEventId(identity: String) extends { val id = identity } with Identity {

}