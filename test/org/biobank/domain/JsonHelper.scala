package org.biobank.domain

import org.biobank.dto._
import org.biobank.domain._
import org.biobank.domain.user._
import org.biobank.domain.study._
import org.biobank.domain.centre._
import org.biobank.infrastructure._

import play.api.libs.json._
import org.scalatest.Assertions._
import com.github.nscala_time.time.Imports._
import org.scalatest._

object JsonHelper extends MustMatchers {
  import org.biobank.infrastructure.JsonUtils._

  private def compareEntity[T <: IdentifiedDomainObject[_]](json: JsValue, entity: ConcurrencySafeEntity[T])  = {
    (json \ "id").as[String]    mustBe (entity.id.toString)
    (json \ "version").as[Long] mustBe (entity.version)

    ((json \ "timeAdded").as[DateTime] to entity.timeAdded).millis must be < 1000L

    (json \ "timeModified").as[Option[DateTime]] match {
      case Some(jsonTimeModified) => {
        entity.timeModified match {
          case Some(entityTimeModified) => (jsonTimeModified to entityTimeModified).millis must be < 1000L
          case None => fail("entity does has no time modified")
        }
      }
      case None => {
        entity.timeModified match {
          case Some(entityTimeModified) => fail("json object has no time modified")
          case None => // test passes
        }
      }
    }
  }

  def compareObj(json: JsValue, user: User)  = {
    compareEntity(json, user)
    (json \ "name").as[String]              mustBe (user.name)
    (json \ "email").as[String]             mustBe (user.email)
    (json \ "avatarUrl").as[Option[String]] mustBe (user.avatarUrl)
  }

  def compareObj(json: JsValue, study: Study)  = {
    compareEntity(json, study)
    (json \ "name").as[String]                   mustBe (study.name)
    (json \ "description").as[Option[String]]    mustBe (study.description)
    (json \ "status").as[String]                 mustBe (study.status)
  }

  def compareObj(json: JsValue, study: StudyNameDto)  = {
    (json \ "id").as[String] mustBe (study.id)

    (json \ "name").as[String] mustBe (study.name)

    (json \ "status").as[String] mustBe (study.status)
  }

  def compareObj(json: JsValue, specimenGroup: SpecimenGroup) = {
    compareEntity(json, specimenGroup)
    (json \ "studyId").as[String]                     mustBe (specimenGroup.studyId.id)
    (json \ "name").as[String]                        mustBe (specimenGroup.name)
    (json \ "description").as[Option[String]]         mustBe (specimenGroup.description)
    (json \ "units").as[String]                       mustBe (specimenGroup.units)
    (json \ "anatomicalSourceType").as[String]        mustBe (specimenGroup.anatomicalSourceType.toString)
    (json \ "preservationType").as[String]            mustBe (specimenGroup.preservationType.toString)
    (json \ "preservationTemperatureType").as[String] mustBe (specimenGroup.preservationTemperatureType.toString)
    (json \ "specimenType").as[String]                mustBe (specimenGroup.specimenType.toString)
  }

  def compareSgData(json: JsValue, specimenGroupData: CollectionEventTypeSpecimenGroupData) = {
    (json \ "specimenGroupId").as[String] mustBe (specimenGroupData.specimenGroupId)
    (json \ "maxCount").as[Int] mustBe (specimenGroupData.maxCount)
    (json \ "amount").as[Option[BigDecimal]] mustBe (specimenGroupData.amount)
  }

  def compareAnnotData(json: JsValue, annotationTypeData: AnnotationTypeData) = {
    (json \ "annotationTypeId").as[String] mustBe (annotationTypeData.annotationTypeId)
    (json \ "required").as[Boolean] mustBe (annotationTypeData.required)
  }

  def compareObj(json: JsValue, ceventType: CollectionEventType)  = {
    compareEntity(json, ceventType)
    (json \ "studyId").as[String]             mustBe (ceventType.studyId.id)
    (json \ "name").as[String]                mustBe (ceventType.name)
    (json \ "description").as[Option[String]] mustBe (ceventType.description)
    (json \ "recurring").as[Boolean]          mustBe (ceventType.recurring)

    (json \ "specimenGroupData").as[List[JsObject]]  must have size ceventType.specimenGroupData.size
    (json \ "annotationTypeData").as[List[JsObject]] must have size ceventType.annotationTypeData.size

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
    (json \ "studyId").as[String]             mustBe (annotType.studyId.id)
    (json \ "name").as[String]                mustBe (annotType.name)
    (json \ "description").as[Option[String]] mustBe (annotType.description)
    (json \ "valueType").as[String]           mustBe (annotType.valueType.toString)
    (json \ "maxValueCount").as[Option[Int]]  mustBe (annotType.maxValueCount)

    (json \ "options").as[Seq[String]] mustBe (annotType.options)
  }

  def compareObj(json: JsValue, annotType: ParticipantAnnotationType)  = {
    compareEntity(json, annotType)
    (json \ "studyId").as[String]             mustBe (annotType.studyId.id)
    (json \ "name").as[String]                mustBe (annotType.name)
    (json \ "description").as[Option[String]] mustBe (annotType.description)
    (json \ "valueType").as[String]           mustBe (annotType.valueType.toString)
    (json \ "maxValueCount").as[Option[Int]]  mustBe (annotType.maxValueCount)
    (json \ "required").as[Boolean]           mustBe (annotType.required)

    (json \ "options").as[Seq[String]]        mustBe (annotType.options)
  }

  def compareObj(json: JsValue, processingType: ProcessingType)  = {
    compareEntity(json, processingType)
    (json \ "studyId").as[String]             mustBe (processingType.studyId.id)
    (json \ "name").as[String]                mustBe (processingType.name)
    (json \ "description").as[Option[String]] mustBe (processingType.description)
    (json \ "enabled").as[Boolean]            mustBe (processingType.enabled)
  }

  def compareObj(json: JsValue, specimenLinkType: SpecimenLinkType)  = {
    compareEntity(json, specimenLinkType)
      (json \ "processingTypeId").as[String]              mustBe (specimenLinkType.processingTypeId.id)
      (json \ "expectedInputChange").as[BigDecimal]       mustBe (specimenLinkType.expectedInputChange)
      (json \ "expectedOutputChange").as[BigDecimal]      mustBe (specimenLinkType.expectedOutputChange)
      (json \ "inputCount").as[Int]                       mustBe (specimenLinkType.inputCount)
      (json \ "outputCount").as[Int]                      mustBe (specimenLinkType.outputCount)
      (json \ "inputGroupId").as[String]                  mustBe (specimenLinkType.inputGroupId.id)
      (json \ "outputGroupId").as[String]                 mustBe (specimenLinkType.outputGroupId.id)
      (json \ "inputContainerTypeId").as[Option[String]]  mustBe (specimenLinkType.inputContainerTypeId.map(_.id))
      (json \ "outputContainerTypeId").as[Option[String]] mustBe (specimenLinkType.outputContainerTypeId.map(_.id))

    (json \ "annotationTypeData").as[List[JsObject]] must have size specimenLinkType.annotationTypeData.size

    (json \ "annotationTypeData")
      .as[List[JsObject]].zip(specimenLinkType.annotationTypeData).foreach { item =>
      compareAnnotData(item._1, item._2)
    }
  }

  def compareObj(json: JsValue, participant: Participant) = {
    compareEntity(json, participant)
      (json \ "studyId").as[String]  mustBe (participant.studyId.id)
      (json \ "uniqueId").as[String] mustBe (participant.uniqueId)
  }

  def compareObj(json: JsValue, centre: Centre) = {
    compareEntity(json, centre)
      (json \ "name").as[String]                   mustBe (centre.name)
      (json \ "description").as[Option[String]]    mustBe (centre.description)
      (json \ "status").as[String]                 mustBe (centre.status)
  }

  def compareObj(json: JsValue, location: Location) = {
    (json \ "id").as[String]                  mustBe (location.id.id)
    (json \ "name").as[String]                mustBe (location.name)
    (json \ "street").as[String]              mustBe (location.street)
    (json \ "city").as[String]                mustBe (location.city)
    (json \ "province").as[String]            mustBe (location.province)
    (json \ "postalCode").as[String]          mustBe (location.postalCode)
    (json \ "countryIsoCode").as[String]      mustBe (location.countryIsoCode)
    (json \ "poBoxNumber").as[Option[String]] mustBe (location.poBoxNumber)
  }


}
