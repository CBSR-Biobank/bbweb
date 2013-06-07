package domain

import domain._

case class AnnotationOption(
  id: String,
  annotationTypeId: AnnotationTypeId,
  value: String) extends IdentifiedValueObject[String] {
}

object AnnotationOptionIdentityService {

  def nextIdentity: String =
    java.util.UUID.randomUUID.toString.toUpperCase

}