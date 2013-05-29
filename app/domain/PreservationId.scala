package domain

case class PreservationId(identity: String) extends { val id = identity } with Identity {

}