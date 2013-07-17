package domain.study

import domain._

case class CollectionEventTypeAnnotationType(
  annotationTypeId: AnnotationTypeId,
  required: Boolean) extends IdentifiedValueObject[String] {
}
