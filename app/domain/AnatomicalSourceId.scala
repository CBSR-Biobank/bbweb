package domain

case class AnatomicalSourceId(identity: String) extends { val id = identity } with Identity {

}