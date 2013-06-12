package infrastructure.events

import domain._

case class AnnotationOptionsAddedEvent(
  annotationTypeId: AnnotationTypeId, annotationOptionId: String,
  options: Set[String])

case class AnnotationOptionsUpdatedEvent(
  annotationTypeId: AnnotationTypeId, annotationOptionId: String,
  options: Set[String])

case class AnnotationOptionsRemovedEvent(
  annotationTypeId: AnnotationTypeId, annotationOptionId: String)