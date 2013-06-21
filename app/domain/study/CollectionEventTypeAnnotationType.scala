package domain.study

import domain._

case class CollectionEventTypeAnnotationType(
  id: String,
  collectionEventTypeId: CollectionEventTypeId,
  annotationTypeId: AnnotationTypeId,
  required: Boolean) extends IdentifiedValueObject[String] {
}
