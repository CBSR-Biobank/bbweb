package domain

case class UserId(identity: String) extends { val id = identity } with IdentifiedValueObject[String] {

}

object UserIdentityService {

  def nextIdentity: UserId =
    new UserId(java.util.UUID.randomUUID.toString.toUpperCase)

}
