package org.biobank.domain

import com.typesafe.scalalogging._
import java.time.OffsetDateTime
import org.biobank.TestUtils
import org.biobank.domain.access._
import org.biobank.domain.centre._
import org.biobank.domain.participants.{CollectionEvent, Participant}
import org.biobank.domain.study._
import org.biobank.domain.user._
import org.biobank.dto._
import org.biobank.infrastructure._
import org.scalatest._
import org.biobank.service.centres.CentreLocationInfo
//import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json._

trait JsonHelper extends MustMatchers with OptionValues {

  //private val log: Logger = LoggerFactory.getLogger(this.getClass)

  private def compareEntity[T <: IdentifiedDomainObject[_]](json: JsValue, entity: ConcurrencySafeEntity[T]) = {
    (json \ "id").as[String] mustBe (entity.id.toString)
    (json \ "version").as[Long] mustBe (entity.version)

    TestUtils.checkTimeStamps(entity,
                              (json \ "timeAdded").as[OffsetDateTime],
                              (json \ "timeModified").asOpt[OffsetDateTime])
  }

  def compareObj(json: JsValue, user: User) = {
    compareEntity(json, user)
    (json \ "state").as[String] mustBe (user.state.id)
    (json \ "slug").as[String] mustBe (user.slug)
    (json \ "name").as[String] mustBe (user.name)
    (json \ "email").as[String] mustBe (user.email)
    (json \ "avatarUrl").asOpt[String] mustBe (user.avatarUrl)
  }

  def compareObj[T <: Study](json: JsValue, study: T) = {
    compareEntity(json, study)

    (json \ "slug").as[String] mustBe (study.slug)

    (json \ "name").as[String] mustBe (study.name)

    (json \ "description").asOpt[String] mustBe (study.description)

    (json \ "state").as[String] mustBe (study.state.id)

    val jsonAnnotationTypes = (json \ "annotationTypes").as[List[JsObject]]

    jsonAnnotationTypes must have size (study.annotationTypes.size.toLong)

    jsonAnnotationTypes.foreach { json =>
      val jsonAnnotationTypeId = (json \ "id").as[String]
      val annotType = study.annotationTypes.find { at => at.id.id == jsonAnnotationTypeId }.value
      compareAnnotationType(json, annotType)
    }
  }

  def compareObj(json: JsValue, entityInfo: EntityInfoDto) = {
    (json \ "id").as[String] mustBe (entityInfo.id)

    (json \ "name").as[String] mustBe (entityInfo.name)
  }

  def compareObj(json: JsValue, nameAndStateDto: NameAndStateDto) = {
    (json \ "id").as[String] mustBe (nameAndStateDto.id)

    (json \ "name").as[String] mustBe (nameAndStateDto.name)

    (json \ "state").as[String] mustBe (nameAndStateDto.state)
  }

  def compareObj(json: JsValue, specimenGroup: SpecimenGroup) = {
    compareEntity(json, specimenGroup)
  }

  def compareSpecimenDescription(json: JsValue, specimenDescription: SpecimenDescription): Unit = {
    (json \ "id").as[String]                          mustBe (specimenDescription.id.id)

    (json \ "slug").as[String]                        mustBe (specimenDescription.slug)

    (json \ "name").as[String]                        mustBe (specimenDescription.name)

    (json \ "description").asOpt[String]              mustBe (specimenDescription.description)

    (json \ "units").as[String]                       mustBe (specimenDescription.units)

    (json \ "anatomicalSourceType").as[String]        mustBe (specimenDescription.anatomicalSourceType.toString)

    (json \ "preservationType").as[String]            mustBe (specimenDescription.preservationType.toString)

    (json \ "preservationTemperatureType").as[String] mustBe (specimenDescription.preservationTemperatureType.toString)

    (json \ "specimenType").as[String]                mustBe (specimenDescription.specimenType.toString)

    ()
  }

  def compareCollectionSpecimenDescription(json: JsValue, specimenDescription: CollectionSpecimenDescription): Unit = {
    compareSpecimenDescription(json, specimenDescription)
    (json \ "maxCount").as[Int]         mustBe (specimenDescription.maxCount)

    (json \ "amount").as[BigDecimal] mustBe (specimenDescription.amount)

    ()
  }

  def compareAnnotData(json: JsValue, annotationTypeData: AnnotationTypeData) = {
    (json \ "annotationTypeId").as[String] mustBe (annotationTypeData.annotationTypeId)
    (json \ "required").as[Boolean] mustBe (annotationTypeData.required)
  }

  def compareObj(json: JsValue, ceventType: CollectionEventType) = {
    compareEntity(json, ceventType)
    (json \ "studyId").as[String] mustBe (ceventType.studyId.id)
    (json \ "slug").as[String] mustBe (ceventType.slug)
    (json \ "name").as[String] mustBe (ceventType.name)
    (json \ "description").asOpt[String] mustBe (ceventType.description)
    (json \ "recurring").as[Boolean] mustBe (ceventType.recurring)

    (json \ "specimenDescriptions").as[List[JsObject]] must have size ceventType.specimenDescriptions.size.toLong
    (json \ "annotationTypes").as[List[JsObject]] must have size ceventType.annotationTypes.size.toLong

    (json \ "specimenDescriptions").as[List[JsObject]].foreach { jsItem =>
      val jsonId = (jsItem \ "id").as[String]
      val sg = ceventType.specimenDescriptions.find { x => x.id.id == jsonId }.value
      compareCollectionSpecimenDescription(jsItem, sg)
    }

    (json \ "annotationTypes").as[List[JsObject]].foreach { jsItem =>
      val jsonId = (jsItem \ "id").as[String]
      val at = ceventType.annotationTypes.find { x => x.id.id == jsonId }.value
      compareAnnotationType(jsItem, at)
    }
  }

  def compareAnnotationType(json: JsValue, annotType: AnnotationType) = {
    (json \ "id").as[String]               mustBe (annotType.id.id)
    (json \ "slug").as[String]             mustBe (annotType.slug)
    (json \ "name").as[String]             mustBe (annotType.name)
    (json \ "description").asOpt[String]   mustBe (annotType.description)
    (json \ "valueType").as[String]        mustBe (annotType.valueType.toString)
    (json \ "maxValueCount").asOpt[Int]    mustBe (annotType.maxValueCount)
    (json \ "options").as[Seq[String]]     mustBe (annotType.options)
    (json \ "required").as[Boolean]        mustBe (annotType.required)
  }

  def compareObj(json: JsValue, processingType: ProcessingType) = {
    compareEntity(json, processingType)
    (json \ "studyId").as[String]        mustBe (processingType.studyId.id)
    (json \ "slug").as[String]           mustBe (processingType.slug)
    (json \ "name").as[String]           mustBe (processingType.name)
    (json \ "description").asOpt[String] mustBe (processingType.description)
    (json \ "enabled").as[Boolean]       mustBe (processingType.enabled)
  }

  def compareObj(json: JsValue, specimenLinkType: SpecimenLinkType) = {
    compareEntity(json, specimenLinkType)
    (json \ "processingTypeId").as[String]         mustBe (specimenLinkType.processingTypeId.id)
    (json \ "expectedInputChange").as[BigDecimal]  mustBe (specimenLinkType.expectedInputChange)
    (json \ "expectedOutputChange").as[BigDecimal] mustBe (specimenLinkType.expectedOutputChange)
    (json \ "inputCount").as[Int]                  mustBe (specimenLinkType.inputCount)
    (json \ "outputCount").as[Int]                 mustBe (specimenLinkType.outputCount)
    (json \ "inputGroupId").as[String]             mustBe (specimenLinkType.inputGroupId.id)
    (json \ "outputGroupId").as[String]            mustBe (specimenLinkType.outputGroupId.id)
    (json \ "inputContainerTypeId").asOpt[String]  mustBe (specimenLinkType.inputContainerTypeId.map(_.id))
    (json \ "outputContainerTypeId").asOpt[String] mustBe (specimenLinkType.outputContainerTypeId.map(_.id))

    (json \ "annotationTypeData").as[List[JsObject]] must have size specimenLinkType.annotationTypeData.size.toLong

    (json \ "annotationTypeData")
      .as[List[JsObject]].zip(specimenLinkType.annotationTypeData).foreach { item =>
        compareAnnotData(item._1, item._2)
      }
  }

  def compareAnnotation(json: JsValue, annotation: Annotation): Unit = {
    (json \ "annotationTypeId").as[String] mustBe (annotation.annotationTypeId.id)
    (json \ "stringValue").asOpt[String] mustBe (annotation.stringValue)
    (json \ "numberValue").asOpt[String] mustBe (annotation.numberValue)

    (json \ "selectedValues").as[List[String]] must have size annotation.selectedValues.size.toLong

    (json \ "selectedValues").as[List[String]].foreach { svJson =>
      annotation.selectedValues.contains(svJson)
    }
  }

  def compareObj(json: JsValue, participant: Participant) = {
    compareEntity(json, participant)
    (json \ "studyId").as[String] mustBe (participant.studyId.id)
    (json \ "uniqueId").as[String] mustBe (participant.uniqueId)

    (json \ "annotations").as[List[JsObject]] must have size participant.annotations.size.toLong

    (json \ "annotations").as[List[JsObject]].foreach { jsAnnotation =>
      val annotationTypeId = (jsAnnotation \ "annotationTypeId").as[String]
      val participantAnnotation = participant.annotations
        .find(a => a.annotationTypeId.id == annotationTypeId).value
      compareAnnotation(jsAnnotation, participantAnnotation)
    }
  }

  def compareObj(json: JsValue, collectionEvent: CollectionEvent) = {
    compareEntity(json, collectionEvent)
    (json \ "participantId").as[String]         mustBe (collectionEvent.participantId.id)
    (json \ "collectionEventTypeId").as[String] mustBe (collectionEvent.collectionEventTypeId.id)
    (json \ "visitNumber").as[Int]              mustBe (collectionEvent.visitNumber)

    TestUtils.checkTimeStamps((json \ "timeCompleted").as[OffsetDateTime],
                              collectionEvent.timeCompleted,
                              1L)
  }

  def compareObj(json: JsValue, centre: Centre) = {
    compareEntity(json, centre)

    (json \ "state").as[String] mustBe (centre.state.id)

    (json \ "slug").as[String] mustBe (centre.slug)

    (json \ "name").as[String] mustBe (centre.name)

    (json \ "description").asOpt[String] mustBe (centre.description)

    (json \ "studyNames").as[List[JsObject]].foreach { jsName =>
      val jsId = (jsName \ "id").as[String]
      centre.studyIds must contain (StudyId(jsId))
    }

    (json \ "locations").as[List[JsObject]].foreach { jsLocation =>
      val jsId = (jsLocation \ "id").as[String]
      val location = centre.locations.find { x => x.id.id == jsId }.value
      compareLocation(jsLocation, location)
    }
  }

  def compareLocation(json: JsValue, location: Location): Unit = {
    (json \ "id").as[String]             mustBe (location.id.id)

    (json \ "slug").as[String]           mustBe (location.slug)

    (json \ "name").as[String]           mustBe (location.name)

    (json \ "street").as[String]         mustBe (location.street)

    (json \ "city").as[String]           mustBe (location.city)

    (json \ "province").as[String]       mustBe (location.province)

    (json \ "postalCode").as[String]     mustBe (location.postalCode)

    (json \ "countryIsoCode").as[String] mustBe (location.countryIsoCode)

    (json \ "poBoxNumber").asOpt[String] mustBe (location.poBoxNumber)

    ()
  }

  def compareObj(json: JsValue, shipment: Shipment) = {
    compareEntity(json, shipment)
    (json \ "courierName").as[String] mustBe (shipment.courierName)
    (json \ "trackingNumber").as[String] mustBe (shipment.trackingNumber)
    (json \ "fromLocationInfo" \ "locationId").as[String] mustBe (shipment.fromLocationId.id)
    (json \ "toLocationInfo" \ "locationId").as[String] mustBe (shipment.toLocationId.id)
    (json \ "state").as[String] mustBe (shipment.state.toString)

    TestUtils.checkOpionalTime((json \ "timePacked").asOpt[OffsetDateTime], shipment.timePacked)
    TestUtils.checkOpionalTime((json \ "timeSent").asOpt[OffsetDateTime], shipment.timeSent)
    TestUtils.checkOpionalTime((json \ "timeReceived").asOpt[OffsetDateTime], shipment.timeReceived)
    TestUtils.checkOpionalTime((json \ "timeUnpacked").asOpt[OffsetDateTime], shipment.timeUnpacked)
  }

  def annotationTypeToJson(annotType: AnnotationType): JsObject = {
    Json.obj("annotationTypeId" -> annotType.id,
             "slug"             -> annotType.slug,
             "name"             -> annotType.name,
             "description"      -> annotType.description,
             "valueType"        -> annotType.valueType,
             "options"          -> annotType.options,
             "required"         -> annotType.required)
  }

  def annotationTypeToJsonNoId(annotType: AnnotationType): JsObject = {
    annotationTypeToJson(annotType) - "id"
  }

  def collectionSpecimenDescriptionToJson(desc: CollectionSpecimenDescription): JsObject = {
    Json.obj("id"                          -> desc.id,
             "slug"                        -> desc.slug,
             "name"                        -> desc.name,
             "description"                 -> desc.description,
             "units"                       -> desc.units,
             "anatomicalSourceType"        -> desc.anatomicalSourceType.toString,
             "preservationType"            -> desc.preservationType.toString,
             "preservationTemperatureType" -> desc.preservationTemperatureType.toString,
             "specimenType"                -> desc.specimenType.toString,
             "maxCount"                    -> desc.maxCount,
             "amount"                      -> desc.amount)
  }

  def collectionSpecimenDescriptionToJsonNoId(spec: CollectionSpecimenDescription): JsObject = {
    collectionSpecimenDescriptionToJson(spec) - "id"
  }

  def compareCentreLocationInfo(json: JsValue, centreLocatinInfo: CentreLocationInfo) = {
    (json \ "centreId").as[String] mustBe (centreLocatinInfo.centreId)

    (json \ "locationId").as[String] mustBe (centreLocatinInfo.locationId)

    (json \ "name").as[String] mustBe (centreLocatinInfo.name)
  }

  def compareObj(json: JsValue, specimen: SpecimenDto) = {
    compareSpecimenDto(json, specimen)
  }

  def compareSpecimenDto(json: JsValue, specimenDto: SpecimenDto) = {
    (json \ "id").as[String] mustBe (specimenDto.id)
    (json \ "version").as[Long] mustBe (specimenDto.version)
    TestUtils.checkTimeStamps(specimenDto.timeAdded, (json \ "timeAdded").as[OffsetDateTime])
    TestUtils.checkOpionalTimeString(specimenDto.timeModified,
                                     (json \ "timeModified").asOpt[OffsetDateTime])

    (json \ "specimenDescriptionId").as[String] mustBe (specimenDto.specimenDescriptionId)

    compareCentreLocationInfo((json \ "originLocationInfo").as[JsValue], specimenDto.originLocationInfo)
    compareCentreLocationInfo((json \ "locationInfo").as[JsValue], specimenDto.locationInfo)

    (json \ "containerId").asOpt[String]   mustBe (specimenDto.containerId)

    (json \ "positionId").asOpt[String]    mustBe (specimenDto.positionId)

    (json \ "amount").as[BigDecimal]       mustBe (specimenDto.amount)

    (json \ "state").as[String]            mustBe (specimenDto.state.toString)

    TestUtils.checkTimeStamps((json \ "timeCreated").as[OffsetDateTime],
                              specimenDto.timeCreated,
                              1L)
  }

  def compareObj(json: JsValue, dto: ShipmentSpecimenDto) = {
    (json \ "id").as[String] mustBe (dto.id)

    (json \ "version").as[Long] mustBe (dto.version)

    (json \ "shipmentId").as[String] mustBe (dto.shipmentId)

    (json \ "state").as[String] mustBe (dto.state)

    TestUtils.checkTimeStamps(dto.timeAdded, (json \ "timeAdded").as[OffsetDateTime])
    TestUtils.checkOpionalTime(dto.timeModified, (json \ "timeModified").asOpt[OffsetDateTime])

    compareSpecimenDto((json \ "specimen").as[JsValue], dto.specimen)
  }

  def compareObj(json: JsValue, dto: CentreLocationInfo) = {
    (json \ "centreId").as[String] mustBe (dto.centreId)

    (json \ "locationId").as[String] mustBe (dto.locationId)

    (json \ "name").as[String] mustBe (dto.name)
  }

  def compareObj(json: JsValue, role: Role) = {
    compareEntity(json, role)

    (json \ "slug").as[String] mustBe (role.slug)

    (json \ "name").as[String] mustBe (role.name)

    (json \ "description").asOpt[String] mustBe (role.description)

    val jsonUserData = (json \ "userData").as[List[JsObject]]
    jsonUserData must have length (role.userIds.size.toLong)
    jsonUserData.foreach { jsUserInfo =>
      val jsUserId = (jsUserInfo \ "id").as[String]
      role.userIds must contain (UserId(jsUserId))
    }

    val jsonParentData = (json \ "parentData").as[List[JsObject]]
    jsonParentData must have length (role.parentIds.size.toLong)
    jsonParentData.foreach { jsParentInfo =>
      val jsParentId = (jsParentInfo \ "id").as[String]
      role.parentIds must contain (AccessItemId(jsParentId))
    }

    val jsonChildData = (json \ "childData").as[List[JsObject]]
    jsonChildData must have length (role.childrenIds.size.toLong)
    jsonChildData.foreach { jsChildInfo =>
      val jsChildId = (jsChildInfo \ "id").as[String]
      role.childrenIds must contain (AccessItemId(jsChildId))
    }

  }

  def compareObj(json: JsValue, membership: Membership) = {
    compareEntity(json, membership)

    (json \ "slug").as[String] mustBe (membership.slug)

    (json \ "name").as[String] mustBe (membership.name)

    (json \ "description").asOpt[String] mustBe (membership.description)

    val jsonUserData = (json \ "userData").as[List[JsObject]]
    jsonUserData must have length (membership.userIds.size.toLong)
    jsonUserData.foreach { jsUserInfo =>
      val jsUserId = (jsUserInfo \ "id").as[String]
      membership.userIds must contain (UserId(jsUserId))
    }

    (json \ "studyData" \ "allEntities").as[Boolean] must be (membership.studyData.allEntities)

    val jsonStudyData = (json \ "studyData" \ "entityData").as[List[JsObject]]
    jsonStudyData must have length (membership.studyData.ids.size.toLong)
    jsonStudyData.foreach { jsStudyInfo =>
      val jsId = (jsStudyInfo \ "id").as[String]
      membership.studyData.ids must contain (StudyId(jsId))
    }

    (json \ "centreData" \ "allEntities").as[Boolean] must be (membership.centreData.allEntities)

    val jsonCentreData = (json \ "centreData" \ "entityData").as[List[JsObject]]
    jsonCentreData must have length (membership.centreData.ids.size.toLong)
    jsonCentreData.foreach { jsCentreInfo =>
      val jsId = (jsCentreInfo \ "id").as[String]
      membership.centreData.ids must contain (CentreId(jsId))
    }
  }

  protected def compareNameDto[T <: ConcurrencySafeEntity[_] with HasName with HasSlug]
    (json: JsValue, entity: T): Unit = {
    compareObj(json, EntityInfoDto(entity.id.toString, entity.slug, entity.name))
    ()
  }

  protected def compareNameAndStateDto[T <: ConcurrencySafeEntity[_] with HasName with HasSlug with HasState]
    (json: JsValue, entity: T): Unit = {
    compareObj(json, NameAndStateDto(entity.id.toString, entity.slug, entity.name, entity.state.id))
    ()
  }

}
