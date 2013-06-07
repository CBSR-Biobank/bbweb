package domain.study

import domain._

case class CollectionEventTypeAnnotationType(
  id: String,
  collectionEventTypeId: CollectionEventTypeId,
  collectionEventAnnotationTypeId: AnnotationTypeId,
  required: Boolean) extends IdentifiedValueObject[String] {
}

object CollectionEventTypeAnnotationTypeIdentityService {

  def nextIdentity: String =
    java.util.UUID.randomUUID.toString.toUpperCase

}