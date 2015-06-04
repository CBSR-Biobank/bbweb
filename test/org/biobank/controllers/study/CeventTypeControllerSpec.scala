package org.biobank.controllers.study

import org.biobank.fixture._
import org.biobank.domain.study.{ CollectionEventType, CollectionEventAnnotationType, Study, SpecimenGroup }
import org.biobank.fixture.ControllerFixture
import org.biobank.domain.JsonHelper._
import org.biobank.domain.study._

import play.api.test.Helpers._
import play.api.libs.json._
import org.scalatest.Tag
import org.slf4j.LoggerFactory
import org.joda.time.DateTime
import play.api.Play.current

class CeventTypeControllerSpec extends ControllerFixture {

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  def uri(study: Study): String = s"/studies/${study.id.id}/cetypes"

  def uri(study: Study, ceventType: CollectionEventType): String =
    uri(study) + s"/${ceventType.id.id}"

  def uriWithQuery(study: Study, ceventType: CollectionEventType): String =
    uri(study) + s"?cetId=${ceventType.id.id}"

  def uri(study: Study, ceventType: CollectionEventType, version: Long): String =
    uri(study, ceventType) + s"/${version}"

  def cetToAddCmd(cet: CollectionEventType) = {
    Json.obj(
      "studyId"              -> cet.studyId.id,
      "name"                 -> cet.name,
      "description"          -> cet.description,
      "recurring"            -> cet.recurring,
      "specimenGroupData"    -> cet.specimenGroupData.map { sg =>
        Json.obj(
          "specimenGroupId"  -> sg.specimenGroupId,
          "maxCount"         -> sg.maxCount,
          "amount"           -> Some(sg.amount)
        )
      },
      "annotationTypeData"   -> cet.annotationTypeData.map { at =>
        Json.obj(
          "annotationTypeId" -> at.annotationTypeId,
          "required"         -> at.required
        )
      }
    )
  }

  def cetToUpdateCmd(cet: CollectionEventType) =
    cetToAddCmd(cet) ++ Json.obj(
      "id"              -> cet.id.id,
      "expectedVersion" -> Some(cet.version)
    )

  def addOnNonDisabledStudy(study: Study) {
    studyRepository.put(study)

    val sg = factory.createSpecimenGroup.copy(studyId = study.id)
    specimenGroupRepository.put(sg)

    val annotType = factory.createCollectionEventAnnotationType.copy(studyId = study.id)
    collectionEventAnnotationTypeRepository.put(annotType)

    val cet = factory.createCollectionEventType.copy(
      studyId            = study.id,
      specimenGroupData  = List(factory.createCollectionEventTypeSpecimenGroupData),
      annotationTypeData = List(factory.createCollectionEventTypeAnnotationTypeData))

    val json = makeRequest(POST, uri(study), BAD_REQUEST, cetToAddCmd(cet))

    (json \ "status").as[String] must include ("error")
    (json \ "message").as[String] must include ("is not disabled")
  }

  def updateOnNonDisabledStudy(study: Study) {
    studyRepository.put(study)

    val sg = factory.createSpecimenGroup.copy(studyId = study.id)
    specimenGroupRepository.put(sg)

    val annotType = factory.createCollectionEventAnnotationType.copy(studyId = study.id)
    collectionEventAnnotationTypeRepository.put(annotType)

    val cet = factory.createCollectionEventType.copy(studyId = study.id)
    collectionEventTypeRepository.put(cet)

    val cet2 = factory.createCollectionEventType.copy(
      studyId            = study.id,
      id                 = cet.id,
      specimenGroupData  = List(factory.createCollectionEventTypeSpecimenGroupData),
      annotationTypeData = List(factory.createCollectionEventTypeAnnotationTypeData))

    val json = makeRequest(PUT, uri(study, cet2), BAD_REQUEST, cetToUpdateCmd(cet2))

    (json \ "status").as[String] must include ("error")
    (json \ "message").as[String] must include ("is not disabled")
  }

  def removeOnNonDisabledStudy(study: Study) {
    studyRepository.put(study)

    val sg = factory.createSpecimenGroup.copy(studyId = study.id)
    specimenGroupRepository.put(sg)

    val annotType = factory.createCollectionEventAnnotationType.copy(studyId = study.id)
    collectionEventAnnotationTypeRepository.put(annotType)

    val cet = factory.createCollectionEventType.copy(
      specimenGroupData = List(factory.createCollectionEventTypeSpecimenGroupData),
      annotationTypeData = List(factory.createCollectionEventTypeAnnotationTypeData))
    collectionEventTypeRepository.put(cet)

    val json = makeRequest(DELETE, uri(study, cet, cet.version), BAD_REQUEST)

    (json \ "status").as[String] must include ("error")
    (json \ "message").as[String] must include ("is not disabled")
  }

  "Collection Event Type REST API" when {

    "GET /studies/cetypes" must {
      "list none" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val json = makeRequest(GET, uri(study))
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size 0
      }

      "list a single collection event type" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val cet = factory.createCollectionEventType
        collectionEventTypeRepository.put(cet)

        val json = makeRequest(GET, uri(study))
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size 1
        compareObj(jsonList(0), cet)
      }

      "get a single collection event type" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val cet = factory.createCollectionEventType
        collectionEventTypeRepository.put(cet)

        val json = makeRequest(GET, uriWithQuery(study, cet))
        (json \ "status").as[String] must include ("success")
        val jsonObj = (json \ "data").as[JsObject]
        compareObj(jsonObj, cet)
      }

      "list multiple collection event types" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val cet1 = factory.createCollectionEventType.copy(
          specimenGroupData = List(factory.createCollectionEventTypeSpecimenGroupData),
          annotationTypeData = List(factory.createCollectionEventTypeAnnotationTypeData))

        val cet2 = factory.createCollectionEventType.copy(
          specimenGroupData = List(factory.createCollectionEventTypeSpecimenGroupData),
          annotationTypeData = List(factory.createCollectionEventTypeAnnotationTypeData))

        val cetypes = List(cet1, cet2)
        cetypes map { cet => collectionEventTypeRepository.put(cet) }

        val json = makeRequest(GET, uri(study))
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]

        jsonList must have size cetypes.size
        (jsonList zip cetypes).map { item => compareObj(item._1, item._2) }
        ()
      }

      "fail for invalid study id" in {
        val study = factory.createDisabledStudy

        val json = makeRequest(GET, uri(study), BAD_REQUEST)
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("invalid study id")
      }

      "fail for an invalid study ID when using a collection event type id" in {
        val study = factory.createDisabledStudy
        val cet = factory.createCollectionEventType

        val json = makeRequest(GET, uriWithQuery(study, cet), BAD_REQUEST)
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("invalid study id")
      }

      "fail for an invalid collection event type id" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val cet = factory.createCollectionEventType

        val json = makeRequest(GET, uriWithQuery(study, cet), NOT_FOUND)
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("collection event type does not exist")
      }

    }

    "POST /studies/cetypes" must {

      "add a collection event type" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val sg = factory.createSpecimenGroup
        specimenGroupRepository.put(sg)

        val annotType = factory.createCollectionEventAnnotationType
        collectionEventAnnotationTypeRepository.put(annotType)

        val cet = factory.createCollectionEventType.copy(
          specimenGroupData = List(factory.createCollectionEventTypeSpecimenGroupData),
          annotationTypeData = List(factory.createCollectionEventTypeAnnotationTypeData))

        val json = makeRequest(POST, uri(study), json = cetToAddCmd(cet))
        (json \ "status").as[String] must include ("success")
      }

      "not add a collection event type to an enabled study" in {
        addOnNonDisabledStudy(factory.createEnabledStudy)
      }

      "not add a collection event type to an retired study" in {
        addOnNonDisabledStudy(factory.createRetiredStudy)
      }

      "fail when adding and study IDs do not match" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val cet = factory.createCollectionEventType

        val study2 = factory.createDisabledStudy

        val json = makeRequest(POST, uri(study2), BAD_REQUEST, json = cetToAddCmd(cet))
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("study id mismatch")
      }

      "allow adding a collection event type with same name on two different studies" in {
        val cet = factory.createCollectionEventType

        List(factory.createDisabledStudy, factory.createDisabledStudy) foreach { study =>
          studyRepository.put(study)

          val cmdJson = cetToAddCmd(cet.copy(studyId = study.id))
          val json = makeRequest(POST, uri(study), json = cmdJson)
          (json \ "status").as[String] must include ("success")
        }
      }

    }

    "PUT /studies/cetypes" must {

      "update a collection event type" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val sg = factory.createSpecimenGroup
        specimenGroupRepository.put(sg)

        val annotType = factory.createCollectionEventAnnotationType
        collectionEventAnnotationTypeRepository.put(annotType)

        val cet = factory.createCollectionEventType
        collectionEventTypeRepository.put(cet)

        val cet2 = factory.createCollectionEventType.copy(
          id = cet.id,
          specimenGroupData = List(factory.createCollectionEventTypeSpecimenGroupData),
          annotationTypeData = List(factory.createCollectionEventTypeAnnotationTypeData))

        val json = makeRequest(PUT, uri(study, cet2), json = cetToUpdateCmd(cet2))
        (json \ "status").as[String] must include ("success")
      }

      "not update a collection event type on an enabled study" in {
        updateOnNonDisabledStudy(factory.createEnabledStudy)
      }

      "not update a collection event type on an retired study" in {
        updateOnNonDisabledStudy(factory.createRetiredStudy)
      }

      "fail when updating and study IDs do not match" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val cet = factory.createCollectionEventType
        collectionEventTypeRepository.put(cet)

        val study2 = factory.createDisabledStudy

        val json = makeRequest(PUT, uri(study2, cet), BAD_REQUEST, json = cetToUpdateCmd(cet))
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("study id mismatch")
      }

      "fail when updating and annotation type IDs do not match" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val cet = factory.createCollectionEventType
        collectionEventTypeRepository.put(cet)

        val cet2 = factory.createCollectionEventType

        val json = makeRequest(PUT, uri(study, cet2), BAD_REQUEST, json = cetToUpdateCmd(cet))
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("annotation type id mismatch")
      }

      "allow a updating collection event types on two different studies to same name" in {
        val commonName = nameGenerator.next[CollectionEventType]

        (0 until 2).map { x =>
          val study = factory.createDisabledStudy
          studyRepository.put(study)
          val cet = factory.createCollectionEventType
          collectionEventTypeRepository.put(cet)
          (study, cet)
        } foreach { case (study: Study, cet: CollectionEventType) =>
          val cmdJson = cetToUpdateCmd(cet.copy(name = commonName))
          val json = makeRequest(PUT, uri(study, cet), json = cmdJson)
          (json \ "status").as[String] must include ("success")
        }
      }
    }

    "DELETE /studies/cetypes" must {
      "remove a collection event type" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val sg = factory.createSpecimenGroup
        specimenGroupRepository.put(sg)

        val annotType = factory.createCollectionEventAnnotationType
        collectionEventAnnotationTypeRepository.put(annotType)

        val cet = factory.createCollectionEventType.copy(
          specimenGroupData = List(factory.createCollectionEventTypeSpecimenGroupData),
          annotationTypeData = List(factory.createCollectionEventTypeAnnotationTypeData))
        collectionEventTypeRepository.put(cet)

        val json = makeRequest(DELETE, uri(study, cet, cet.version))

        (json \ "status").as[String] must include ("success")
      }

      "not remove a collection event type on an enabled study" in {
        removeOnNonDisabledStudy(factory.createEnabledStudy)
      }

      "not remove a collection event type on an retired study" in {
        removeOnNonDisabledStudy(factory.createRetiredStudy)
      }
    }
  }

}
