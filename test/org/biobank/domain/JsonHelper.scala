package org.biobank.domain

import org.biobank.TestUtils
import org.biobank.dto._
import org.biobank.domain.user._
import org.biobank.domain.study._
import org.biobank.domain.participants.{
  CollectionEvent,
  Participant,
  Specimen
}
import org.biobank.domain.centre._
import org.biobank.infrastructure._

import play.api.libs.json._
import com.github.nscala_time.time.Imports._
import org.scalatest._
import com.typesafe.scalalogging._

trait JsonHelper extends MustMatchers with OptionValues {
  import org.biobank.infrastructure.JsonUtils._

  val log: Logger

  private def compareEntity[T <: IdentifiedDomainObject[_]](json: JsValue, entity: ConcurrencySafeEntity[T]) = {
    (json \ "id").as[String] mustBe (entity.id.toString)
    (json \ "version").as[Long] mustBe (entity.version)

    TestUtils.checkTimeStamps(entity,
                              (json \ "timeAdded").as[DateTime],
                              (json \ "timeModified").asOpt[DateTime])
  }

  def compareObj(json: JsValue, user: User) = {
    compareEntity(json, user)
    (json \ "name").as[String] mustBe (user.name)
    (json \ "email").as[String] mustBe (user.email)
    (json \ "avatarUrl").asOpt[String] mustBe (user.avatarUrl)
  }

  def compareObj[T <: Study](json: JsValue, study: T) = {
    compareEntity(json, study)

    (json \ "name").as[String] mustBe (study.name)

    (json \ "description").asOpt[String] mustBe (study.description)

    (json \ "status").as[String] mustBe (study.getClass.getSimpleName)

    val jsonAnnotationTypes = (json \ "annotationTypes").as[List[JsObject]]

    jsonAnnotationTypes must have size (study.annotationTypes.size)

    jsonAnnotationTypes.foreach { json =>
      val jsonUniqueId = (json \ "uniqueId").as[String]
      study.annotationTypes.find { at => at.uniqueId == jsonUniqueId }.fold {
        fail(s"could not find annotation type in study: $jsonUniqueId")
      } {
        compareAnnotationType(json, _)
      }
    }
  }

  def compareObj(json: JsValue, nameDto: NameDto) = {
    (json \ "id").as[String] mustBe (nameDto.id)

    (json \ "name").as[String] mustBe (nameDto.name)

    (json \ "status").as[String] mustBe (nameDto.status)
  }

  def compareObj(json: JsValue, specimenGroup: SpecimenGroup) = {
    compareEntity(json, specimenGroup)
  }

  def compareSpecimenSpec(json: JsValue, specimenSpec: SpecimenSpec): Unit = {
    (json \ "uniqueId").as[String]                    mustBe (specimenSpec.uniqueId)
    (json \ "name").as[String]                        mustBe (specimenSpec.name)
    (json \ "description").asOpt[String]              mustBe (specimenSpec.description)
    (json \ "units").as[String]                       mustBe (specimenSpec.units)
    (json \ "anatomicalSourceType").as[String]        mustBe (specimenSpec.anatomicalSourceType.toString)
    (json \ "preservationType").as[String]            mustBe (specimenSpec.preservationType.toString)
    (json \ "preservationTemperatureType").as[String] mustBe (specimenSpec.preservationTemperatureType.toString)
    (json \ "specimenType").as[String]                mustBe (specimenSpec.specimenType.toString)
  }

  def compareCollectionSpecimenSpec(json: JsValue, specimenSpec: CollectionSpecimenSpec): Unit = {
    compareSpecimenSpec(json, specimenSpec)
    (json \ "maxCount").as[Int]         mustBe (specimenSpec.maxCount)
    (json \ "amount").asOpt[BigDecimal] mustBe (specimenSpec.amount)
  }

  def compareAnnotData(json: JsValue, annotationTypeData: AnnotationTypeData) = {
    (json \ "annotationTypeId").as[String] mustBe (annotationTypeData.annotationTypeId)
    (json \ "required").as[Boolean] mustBe (annotationTypeData.required)
  }

  def compareObj(json: JsValue, ceventType: CollectionEventType) = {
    compareEntity(json, ceventType)
    (json \ "studyId").as[String] mustBe (ceventType.studyId.id)
    (json \ "name").as[String] mustBe (ceventType.name)
    (json \ "description").asOpt[String] mustBe (ceventType.description)
    (json \ "recurring").as[Boolean] mustBe (ceventType.recurring)

    (json \ "specimenSpecs").as[List[JsObject]] must have size ceventType.specimenSpecs.size
    (json \ "annotationTypes").as[List[JsObject]] must have size ceventType.annotationTypes.size

    (json \ "specimenSpecs").as[List[JsObject]].foreach { jsItem =>
      val jsonId = (jsItem \ "uniqueId").as[String]
      val sg = ceventType.specimenSpecs.find { x => x.uniqueId == jsonId }
      sg mustBe defined
      sg.map { compareCollectionSpecimenSpec(jsItem, _) }
    }

    (json \ "annotationTypes").as[List[JsObject]].foreach { jsItem =>
      val jsonId = (jsItem \ "uniqueId").as[String]
      val at = ceventType.annotationTypes.find { x => x.uniqueId == jsonId }
      at mustBe defined
      at.map { compareAnnotationType(jsItem, _) }
    }
  }

  def compareAnnotationType(json: JsValue, annotType: AnnotationType) = {
    (json \ "uniqueId").as[String]       mustBe (annotType.uniqueId)
    (json \ "name").as[String]           mustBe (annotType.name)
    (json \ "description").asOpt[String] mustBe (annotType.description)
    (json \ "valueType").as[String]      mustBe (annotType.valueType.toString)
    (json \ "maxValueCount").asOpt[Int]  mustBe (annotType.maxValueCount)
    (json \ "options").as[Seq[String]]   mustBe (annotType.options)
    (json \ "required").as[Boolean]      mustBe (annotType.required)
  }

  def compareObj(json: JsValue, processingType: ProcessingType) = {
    compareEntity(json, processingType)
    (json \ "studyId").as[String] mustBe (processingType.studyId.id)
    (json \ "name").as[String] mustBe (processingType.name)
    (json \ "description").asOpt[String] mustBe (processingType.description)
    (json \ "enabled").as[Boolean] mustBe (processingType.enabled)
  }

  def compareObj(json: JsValue, specimenLinkType: SpecimenLinkType) = {
    compareEntity(json, specimenLinkType)
    (json \ "processingTypeId").as[String] mustBe (specimenLinkType.processingTypeId.id)
    (json \ "expectedInputChange").as[BigDecimal] mustBe (specimenLinkType.expectedInputChange)
    (json \ "expectedOutputChange").as[BigDecimal] mustBe (specimenLinkType.expectedOutputChange)
    (json \ "inputCount").as[Int] mustBe (specimenLinkType.inputCount)
    (json \ "outputCount").as[Int] mustBe (specimenLinkType.outputCount)
    (json \ "inputGroupId").as[String] mustBe (specimenLinkType.inputGroupId.id)
    (json \ "outputGroupId").as[String] mustBe (specimenLinkType.outputGroupId.id)
    (json \ "inputContainerTypeId").asOpt[String] mustBe (specimenLinkType.inputContainerTypeId.map(_.id))
    (json \ "outputContainerTypeId").asOpt[String] mustBe (specimenLinkType.outputContainerTypeId.map(_.id))

    (json \ "annotationTypeData").as[List[JsObject]] must have size specimenLinkType.annotationTypeData.size

    (json \ "annotationTypeData")
      .as[List[JsObject]].zip(specimenLinkType.annotationTypeData).foreach { item =>
        compareAnnotData(item._1, item._2)
      }
  }

  def compareAnnotation(json: JsValue, annotation: Annotation): Unit = {
    (json \ "annotationTypeId").as[String] mustBe (annotation.annotationTypeId)
    (json \ "stringValue").asOpt[String] mustBe (annotation.stringValue)
    (json \ "numberValue").asOpt[String] mustBe (annotation.numberValue)

    (json \ "selectedValues").as[List[String]] must have size annotation.selectedValues.size

    (json \ "selectedValues").as[List[String]].foreach { svJson =>
      annotation.selectedValues.contains(svJson)
    }
  }

  def compareObj(json: JsValue, participant: Participant) = {
    compareEntity(json, participant)
    (json \ "studyId").as[String] mustBe (participant.studyId.id)
    (json \ "uniqueId").as[String] mustBe (participant.uniqueId)

    (json \ "annotations").as[List[JsObject]] must have size participant.annotations.size

    (json \ "annotations").as[List[JsObject]].foreach { jsAnnotation =>
      val annotationTypeId = (jsAnnotation \ "annotationTypeId").as[String]
      val participantAnnotationMaybe = participant.annotations.find { a  =>
        a.annotationTypeId == annotationTypeId
      }

      participantAnnotationMaybe match {
        case Some(participantAnnotation) =>
          compareAnnotation(jsAnnotation, participantAnnotation)
        case None =>
          fail(s"annotation with annotationTypeId not found on participant: $annotationTypeId")
      }
    }
  }

  def compareObj(json: JsValue, collectionEvent: CollectionEvent) = {
    compareEntity(json, collectionEvent)
    (json \ "participantId").as[String]         mustBe (collectionEvent.participantId.id)
    (json \ "collectionEventTypeId").as[String] mustBe (collectionEvent.collectionEventTypeId.id)
    (json \ "visitNumber").as[Int]              mustBe (collectionEvent.visitNumber)

    ((json \ "timeCompleted").as[DateTime] to collectionEvent.timeCompleted).millis must be < 1000L
  }

  def compareObj(json: JsValue, specimen: Specimen) = {
    compareEntity(json, specimen)

    (json \ "specimenSpecId").as[String]   mustBe (specimen.specimenSpecId)

    (json \ "originLocationId").as[String] mustBe (specimen.originLocationId)

    (json \ "locationId").as[String]       mustBe (specimen.locationId)

    (json \ "containerId").asOpt[String]   mustBe (specimen.containerId)

    (json \ "positionId").asOpt[String]    mustBe (specimen.positionId)

    (json \ "amount").as[BigDecimal]       mustBe (specimen.amount)

    (json \ "status").as[String]           mustBe (specimen.getClass.getSimpleName)

    ((json \ "timeCreated").as[DateTime] to specimen.timeCreated).millis must be < 1000L
  }

  def compareObj(json: JsValue, centre: Centre) = {
    compareEntity(json, centre)

    (json \ "name").as[String] mustBe (centre.name)
    (json \ "description").asOpt[String] mustBe (centre.description)
    (json \ "status").as[String] mustBe (centre.getClass.getSimpleName)

    (json \ "studyIds").as[List[String]].foreach { jsStudyId =>
      centre.studyIds must contain (StudyId(jsStudyId))
    }

    (json \ "locations").as[List[JsObject]].foreach { jsLocation =>
      val jsLocationId = (jsLocation \ "uniqueId").as[String]
      centre.locations.find { x => x.uniqueId == jsLocationId }
        .fold { fail(s"annotation with id not found on centre: $jsLocationId")
      } { location => compareLocation(jsLocation, location) }
    }
  }

  def compareLocation(json: JsValue, location: Location): Unit = {
    (json \ "uniqueId").as[String]       mustBe (location.uniqueId)
    (json \ "name").as[String]           mustBe (location.name)
    (json \ "street").as[String]         mustBe (location.street)
    (json \ "city").as[String]           mustBe (location.city)
    (json \ "province").as[String]       mustBe (location.province)
    (json \ "postalCode").as[String]     mustBe (location.postalCode)
    (json \ "countryIsoCode").as[String] mustBe (location.countryIsoCode)
    (json \ "poBoxNumber").asOpt[String] mustBe (location.poBoxNumber)
  }

  def compareObj(json: JsValue, shipment: Shipment) = {
    compareEntity(json, shipment)
    (json \ "courierName").as[String] mustBe (shipment.courierName)
    (json \ "trackingNumber").as[String] mustBe (shipment.trackingNumber)
    (json \ "fromLocationId").as[String] mustBe (shipment.fromLocationId)
    (json \ "toLocationId").as[String] mustBe (shipment.toLocationId)

    TestUtils.checkOpionalTime((json \ "timePacked").asOpt[DateTime], shipment.timePacked)
    TestUtils.checkOpionalTime((json \ "timeSent").asOpt[DateTime], shipment.timeSent)
    TestUtils.checkOpionalTime((json \ "timeReceived").asOpt[DateTime], shipment.timeReceived)
    TestUtils.checkOpionalTime((json \ "timeUnpacked").asOpt[DateTime], shipment.timeUnpacked)
  }

  def annotationTypeToJson(annotType: AnnotationType): JsObject = {
    Json.obj("uniqueId"    -> annotType.uniqueId,
             "name"        -> annotType.name,
             "description" -> annotType.description,
             "valueType"   -> annotType.valueType,
             "options"     -> annotType.options,
             "required"    -> annotType.required)
  }

  def annotationTypeToJsonNoId(annotType: AnnotationType): JsObject = {
    annotationTypeToJson(annotType) - "id"
  }

  def collectionSpecimenSpecToJson(spec: CollectionSpecimenSpec): JsObject = {
    Json.obj("uniqueId"                    -> spec.uniqueId,
             "name"                        -> spec.name,
             "description"                 -> spec.description,
             "units"                       -> spec.units,
             "anatomicalSourceType"        -> spec.anatomicalSourceType.toString,
             "preservationType"            -> spec.preservationType.toString,
             "preservationTemperatureType" -> spec.preservationTemperatureType.toString,
             "specimenType"                -> spec.specimenType.toString,
             "maxCount"                    -> spec.maxCount,
             "amount"                      -> spec.amount)
  }

  def collectionSpecimenSpecToJsonNoId(spec: CollectionSpecimenSpec): JsObject = {
    collectionSpecimenSpecToJson(spec) - "id"
  }

}
