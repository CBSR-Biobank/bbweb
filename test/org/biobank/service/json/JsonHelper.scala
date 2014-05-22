package org.biobank.service.json

import org.biobank.domain.study._
import org.biobank.infrastructure._

import org.scalatest.Matchers
import play.api.libs.json._
import org.scalatest.Assertions._
import com.github.nscala_time.time.Imports._

object JsonHelper extends Matchers {
  import org.biobank.service.json.JsonUtils._

  def compareObj(json: JsValue, study: Study)  = {
    (json \ "id").as[String]                     should be (study.id.id)
    (json \ "version").as[Long]                  should be (study.version)
    (json \ "name").as[String]                   should be (study.name)
    (json \ "description").as[Option[String]]    should be (study.description)
    (json \ "status").as[String]                 should be (study.status)

    ((json \ "addedDate").as[DateTime] to study.addedDate).millis should be < 1000L
    (json \ "lastUpdateDate").as[Option[DateTime]] map { dateTime =>
      (dateTime to study.lastUpdateDate.get).millis should be < 1000L
    }
  }

  def compareObj(json: JsValue, specimenGroup: SpecimenGroup) = {
    (json \ "studyId").as[String]                     should be (specimenGroup.studyId.id)
    (json \ "id").as[String]                          should be (specimenGroup.id.id)
    (json \ "version").as[Long]                       should be (specimenGroup.version)
    (json \ "name").as[String]                        should be (specimenGroup.name)
    (json \ "description").as[Option[String]]         should be (specimenGroup.description)
    (json \ "units").as[String]                       should be (specimenGroup.units)
    (json \ "anatomicalSourceType").as[String]        should be (specimenGroup.anatomicalSourceType.toString)
    (json \ "preservationType").as[String]            should be (specimenGroup.preservationType.toString)
    (json \ "preservationTemperatureType").as[String] should be (specimenGroup.preservationTemperatureType.toString)
    (json \ "specimenType").as[String]                should be (specimenGroup.specimenType.toString)

    ((json \ "addedDate").as[DateTime] to specimenGroup.addedDate).millis should be < 1000L
    (json \ "lastUpdateDate").as[Option[DateTime]] map { dateTime =>
      (dateTime to specimenGroup.lastUpdateDate.get).millis should be < 1000L
    }
  }

  def compareSgData(json: JsValue, specimenGroupData: CollectionEventTypeSpecimenGroupData) = {
    (json \ "specimenGroupId").as[String] should be (specimenGroupData.specimenGroupId)
    (json \ "maxCount").as[Int] should be (specimenGroupData.maxCount)
    (json \ "amount").as[Option[BigDecimal]] should be (specimenGroupData.amount)
  }

  def compareAnnotData(json: JsValue, annotationTypeData: AnnotationTypeData) = {
    (json \ "annotationTypeId").as[String] should be (annotationTypeData.annotationTypeId)
    (json \ "required").as[Boolean] should be (annotationTypeData.required)
  }

  def compareObj(json: JsValue, ceventType: CollectionEventType)  = {
    (json \ "studyId").as[String]             should be (ceventType.studyId.id)
    (json \ "id").as[String]                  should be (ceventType.id.id)
    (json \ "version").as[Long]               should be (ceventType.version)
    (json \ "name").as[String]                should be (ceventType.name)
    (json \ "description").as[Option[String]] should be (ceventType.description)
    (json \ "recurring").as[Boolean]          should be (ceventType.recurring)

    (json \ "specimenGroupData").as[List[JsObject]]  should have size ceventType.specimenGroupData.size
    (json \ "annotationTypeData").as[List[JsObject]] should have size ceventType.annotationTypeData.size

    (json \ "specimenGroupData")
      .as[List[JsObject]].zip(ceventType.specimenGroupData).foreach { item =>
      compareSgData(item._1, item._2)
    }

    (json \ "annotationTypeData")
      .as[List[JsObject]].zip(ceventType.annotationTypeData).foreach { item =>
      compareAnnotData(item._1, item._2)
    }

    ((json \ "addedDate").as[DateTime] to ceventType.addedDate).millis should be < 1000L
    (json \ "lastUpdateDate").as[Option[DateTime]] map { dateTime =>
      (dateTime to ceventType.lastUpdateDate.get).millis should be < 1000L
    }
  }

  def compareObj(json: JsValue, annotType: CollectionEventAnnotationType)  = {
    (json \ "studyId").as[String]             should be (annotType.studyId.id)
    (json \ "id").as[String]                  should be (annotType.id.id)
    (json \ "version").as[Long]               should be (annotType.version)
    (json \ "name").as[String]                should be (annotType.name)
    (json \ "description").as[Option[String]] should be (annotType.description)
    (json \ "valueType").as[String]           should be (annotType.valueType.toString)
    (json \ "maxValueCount").as[Option[Int]]  should be (annotType.maxValueCount)

    (json \ "options").as[Option[Map[String, String]]] should be (annotType.options)

    ((json \ "addedDate").as[DateTime] to annotType.addedDate).millis should be < 1000L
    (json \ "lastUpdateDate").as[Option[DateTime]] map { dateTime =>
      (dateTime to annotType.lastUpdateDate.get).millis should be < 1000L
    }
  }

}
