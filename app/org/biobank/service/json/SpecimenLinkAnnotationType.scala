package org.biobank.service.json

import org.biobank.infrastructure._
import org.biobank.domain.study._
import org.biobank.domain._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.domain.AnnotationValueType._

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import org.joda.time.DateTime
import scala.collection.immutable.Map

object SpecimenLinkAnnotationType {
  import JsonUtils._
  import EnumUtils._
  import StudyId._
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
    (__ \ "type").read[String](Reads.verifying[String](_ == "AddSpecimenLinkAnnotationTypeCmd")) andKeep
      (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String](minLength[String](2)) and
      (__ \ "valueType").read[AnnotationValueType] and
      (__ \ "maxValueCount").readNullable[Int] and
      (__ \ "options").readNullable[Map[String, String]]
  )((studyId, name, description, valueType, maxValueCount, options) =>
    AddSpecimenLinkAnnotationTypeCmd(studyId, name, description, valueType, maxValueCount, options))

  implicit val updateSpecimenLinkAnnotationTypeCmdReads: Reads[UpdateSpecimenLinkAnnotationTypeCmd] = (
    (__ \ "type").read[String](Reads.verifying[String](_ == "UpdateSpecimenLinkAnnotationTypeCmd")) andKeep
      (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").readNullable[Long](min[Long](0)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String](minLength[String](2)) and
      (__ \ "valueType").read[AnnotationValueType] and
      (__ \ "maxValueCount").readNullable[Int] and
      (__ \ "options").readNullable[Map[String, String]]
  )((studyId, id, expectedVersion, name, description, valueType, maxValueCount, options) =>
    UpdateSpecimenLinkAnnotationTypeCmd(studyId, id, expectedVersion, name, description, valueType, maxValueCount, options))

  implicit val removeSpecimenLinkAnnotationTypeCmdReads: Reads[RemoveSpecimenLinkAnnotationTypeCmd] = (
    (__ \ "type").read[String](Reads.verifying[String](_ == "RemoveSpecimenLinkAnnotationTypeCmd")) andKeep
      (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").readNullable[Long](min[Long](0))
  )((studyId, id, expectedVersion) => RemoveSpecimenLinkAnnotationTypeCmd(studyId, id, expectedVersion))
}
