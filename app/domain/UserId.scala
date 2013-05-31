package domain

case class UserId(identity: String) extends { val id = identity } with Identity {

}