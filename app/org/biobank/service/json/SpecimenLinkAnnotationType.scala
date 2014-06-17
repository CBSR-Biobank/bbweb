package org.biobank.service.json

import org.biobank.infrastructure._
import org.biobank.domain.study._
import org.biobank.domain._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.domain.AnnotationValueType._

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import org.joda.time.DateTime
import scala.collection.immutable.Map

object SpecimenLinkAnnotationType {
  import JsonUtils._
  import EnumUtils._
  import Study._
  import StudyAnnotationType._

  implicit val specimenLinkAnnotationTypeWrites: Writes[SpecimenLinkAnnotationType] = (
      (__ \ "studyId").write[StudyId] and
      (__ \ "id").write[AnnotationTypeId] and
      (__ \ "version").write[Long] and
      (__ \ "addedDate").write[DateTime] and
      (__ \ "lastUpdateDate").write[Option[DateTime]] and
      (__ \ "name").write[String] and
      (__ \ "description").write[Option[String]] and
      (__ \ "valueType").write[AnnotationValueType] and
      (__ \ "maxValueCount").write[Option[Int]] and
      (__ \ "options").write[Option[Map[String, String]]]
  )(unlift(org.biobank.domain.study.SpecimenLinkAnnotationType.unapply))

  implicit val addSpecimenLinkAnnotationTypeCmdReads: Reads[AddSpecimenLinkAnnotationTypeCmd] = (
    (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String] and
      (__ \ "valueType").read[AnnotationValueType] and
      (__ \ "maxValueCount").readNullable[Int] and
      (__ \ "options").readNullable[Map[String, String]]
  )(AddSpecimenLinkAnnotationTypeCmd.apply _)

  implicit val updateSpecimenLinkAnnotationTypeCmdReads: Reads[UpdateSpecimenLinkAnnotationTypeCmd] = (
    (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").readNullable[Long](min[Long](0)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String] and
      (__ \ "valueType").read[AnnotationValueType] and
      (__ \ "maxValueCount").readNullable[Int] and
      (__ \ "options").readNullable[Map[String, String]]
  )(UpdateSpecimenLinkAnnotationTypeCmd.apply _)

  implicit val removeSpecimenLinkAnnotationTypeCmdReads: Reads[RemoveSpecimenLinkAnnotationTypeCmd] = (
    (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").readNullable[Long](min[Long](0))
  )(RemoveSpecimenLinkAnnotationTypeCmd.apply _)

  implicit val specimenLinkAnnotationTypeAddedEventWrites: Writes[SpecimenLinkAnnotationTypeAddedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "annotationTypeId").write[String] and
      (__ \ "dateTime").write[DateTime] and
      (__ \ "name").write[String] and
      (__ \ "description").writeNullable[String] and
      (__ \ "valueType").write[AnnotationValueType] and
      (__ \ "maxValueCount").writeNullable[Int] and
      (__ \ "options").write[Option[Map[String, String]]]
  )(unlift(SpecimenLinkAnnotationTypeAddedEvent.unapply))

  implicit val specimenLinkAnnotationTypeUpdatedEventWrites: Writes[SpecimenLinkAnnotationTypeUpdatedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "annotationTypeId").write[String] and
      (__ \ "version").write[Long] and
      (__ \ "dateTime").write[DateTime] and
      (__ \ "name").write[String] and
      (__ \ "description").writeNullable[String] and
      (__ \ "valueType").write[AnnotationValueType] and
      (__ \ "maxValueCount").writeNullable[Int] and
      (__ \ "options").write[Option[Map[String, String]]]
  )(unlift(SpecimenLinkAnnotationTypeUpdatedEvent.unapply))

  implicit val specimenLinkAnnotationTypeRemovedEventWrites: Writes[SpecimenLinkAnnotationTypeRemovedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "annotationTypeId").write[String]
  )(unlift(SpecimenLinkAnnotationTypeRemovedEvent.unapply))
}
