package domain

class AnnotationTypeId(identity: String) extends { val id = identity } with IdentifiedValueObject[String] {

}

object AnnotationTypeIdentityService {

  def nextIdentity: AnnotationTypeId =
    new AnnotationTypeId(java.util.UUID.randomUUID.toString.toUpperCase)

}