package org.biobank.service.json

import org.biobank.domain._
import org.biobank.domain.user._
import org.biobank.domain.study._
import org.biobank.domain.centre._
import org.biobank.infrastructure._

import org.scalatest.Matchers
import play.api.libs.json._
import org.scalatest.Assertions._
import com.github.nscala_time.time.Imports._

object JsonHelper extends Matchers {
  import org.biobank.infrastructure.JsonUtils._

  private def compareEntity[T <: IdentifiedDomainObject[_]](json: JsValue, entity: ConcurrencySafeEntity[T])  = {
    (json \ "id").as[String]    should be (entity.id.toString)
    (json \ "version").as[Long] should be (entity.version)

    ((json \ "addedDate").as[DateTime] to entity.addedDate).millis should be < 1000L
    (json \ "lastUpdateDate").as[Option[DateTime]] map { dateTime =>
      (dateTime to entity.lastUpdateDate.get).millis should be < 1000L
    }
  }

  def compareObj(json: JsValue, user: User)  = {
    compareEntity(json, user)
    (json \ "name").as[String]              should be (user.name)
    (json \ "email").as[String]             should be (user.email)
    (json \ "avatarUrl").as[Option[String]] should be (user.avatarUrl)
  }

  def compareObj(json: JsValue, study: Study)  = {
    compareEntity(json, study)
    (json \ "name").as[String]                   should be (study.name)
    (json \ "description").as[Option[String]]    should be (study.description)
    (json \ "status").as[String]                 should be (study.status)
  }

  def compareObj(json: JsValue, specimenGroup: SpecimenGroup) = {
    compareEntity(json, specimenGroup)
    (json \ "studyId").as[String]                     should be (specimenGroup.studyId.id)
    (json \ "name").as[String]                        should be (specimenGroup.name)
    (json \ "description").as[Option[String]]         should be (specimenGroup.description)
    (json \ "units").as[String]                       should be (specimenGroup.units)
    (json \ "anatomicalSourceType").as[String]        should be (specimenGroup.anatomicalSourceType.toString)
    (json \ "preservationType").as[String]            should be (specimenGroup.preservationType.toString)
    (json \ "preservationTemperatureType").as[String] should be (specimenGroup.preservationTemperatureType.toString)
    (json \ "specimenType").as[String]                should be (specimenGroup.specimenType.toString)
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
    compareEntity(json, ceventType)
    (json \ "studyId").as[String]             should be (ceventType.studyId.id)
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
  }

  def compareObj(json: JsValue, annotType: StudyAnnotationType)  = {
    compareEntity(json, annotType)
    (json \ "studyId").as[String]             should be (annotType.studyId.id)
    (json \ "name").as[String]                should be (annotType.name)
    (json \ "description").as[Option[String]] should be (annotType.description)
    (json \ "valueType").as[String]           should be (annotType.valueType.toString)
    (json \ "maxValueCount").as[Option[Int]]  should be (annotType.maxValueCount)

    (json \ "options").as[Option[Seq[String]]] should be (annotType.options)
  }

  def compareObj(json: JsValue, annotType: ParticipantAnnotationType)  = {
    compareEntity(json, annotType)
    (json \ "studyId").as[String]             should be (annotType.studyId.id)
    (json \ "name").as[String]                should be (annotType.name)
    (json \ "description").as[Option[String]] should be (annotType.description)
    (json \ "valueType").as[String]           should be (annotType.valueType.toString)
    (json \ "maxValueCount").as[Option[Int]]  should be (annotType.maxValueCount)
    (json \ "required").as[Boolean]           should be (annotType.required)

    (json \ "options").as[Option[Seq[String]]] should be (annotType.options)
  }

  def compareObj(json: JsValue, processingType: ProcessingType)  = {
    compareEntity(json, processingType)
    (json \ "studyId").as[String]             should be (processingType.studyId.id)
    (json \ "name").as[String]                should be (processingType.name)
    (json \ "description").as[Option[String]] should be (processingType.description)
    (json \ "enabled").as[Boolean]            should be (processingType.enabled)
  }

  def compareObj(json: JsValue, specimenLinkType: SpecimenLinkType)  = {
    compareEntity(json, specimenLinkType)
      (json \ "processingTypeId").as[String]              should be (specimenLinkType.processingTypeId.id)
      (json \ "expectedInputChange").as[BigDecimal]       should be (specimenLinkType.expectedInputChange)
      (json \ "expectedOutputChange").as[BigDecimal]      should be (specimenLinkType.expectedOutputChange)
      (json \ "inputCount").as[Int]                       should be (specimenLinkType.inputCount)
      (json \ "outputCount").as[Int]                      should be (specimenLinkType.outputCount)
      (json \ "inputGroupId").as[String]                  should be (specimenLinkType.inputGroupId.id)
      (json \ "outputGroupId").as[String]                 should be (specimenLinkType.outputGroupId.id)
      (json \ "inputContainerTypeId").as[Option[String]]  should be (specimenLinkType.inputContainerTypeId.map(_.id))
      (json \ "outputContainerTypeId").as[Option[String]] should be (specimenLinkType.outputContainerTypeId.map(_.id))

    (json \ "annotationTypeData").as[List[JsObject]] should have size specimenLinkType.annotationTypeData.size

    (json \ "annotationTypeData")
      .as[List[JsObject]].zip(specimenLinkType.annotationTypeData).foreach { item =>
      compareAnnotData(item._1, item._2)
    }
  }

  def compareObj(json: JsValue, centre: Centre) = {
    compareEntity(json, centre)
      (json \ "name").as[String]                   should be (centre.name)
      (json \ "description").as[Option[String]]    should be (centre.description)
      (json \ "status").as[String]                 should be (centre.status)
  }

  def compareObj(json: JsValue, location: Location) = {
    (json \ "id").as[String]                  should be (location.id.id)
    (json \ "name").as[String]                should be (location.name)
    (json \ "street").as[String]              should be (location.street)
    (json \ "city").as[String]                should be (location.city)
    (json \ "province").as[String]            should be (location.province)
    (json \ "postalCode").as[String]          should be (location.postalCode)
    (json \ "countryIsoCode").as[String]      should be (location.countryIsoCode)
    (json \ "poBoxNumber").as[Option[String]] should be (location.poBoxNumber)
  }


}
