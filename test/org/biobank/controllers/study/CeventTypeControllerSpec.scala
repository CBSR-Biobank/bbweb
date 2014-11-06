package org.biobank.controllers.study

import org.biobank.fixture._
import org.biobank.domain.study.{ CollectionEventType, CollectionEventAnnotationType, Study, SpecimenGroup }
import org.biobank.fixture.ControllerFixture
import org.biobank.domain.JsonHelper._

import play.api.test.Helpers._
import play.api.test.WithApplication
import play.api.libs.json._
import org.scalatest.Tag
import org.slf4j.LoggerFactory
import org.joda.time.DateTime
import com.typesafe.plugin._
import play.api.Play.current
import org.scalatestplus.play._

class CeventTypeControllerSpec extends ControllerFixture {
  import TestGlobal._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  def uri(study: Study): String = s"/studies/${study.id.id}/cetypes"

  def uri(study: Study, ceventType: CollectionEventType): String =
    uri(study) + s"/${ceventType.id.id}"

  def uriWithQuery(study: Study, ceventType: CollectionEventType): String =
    uri(study) + s"?cetId=${ceventType.id.id}"

  def uri(study: Study, ceventType: CollectionEventType, version: Long): String =
    uri(study, ceventType) + s"/${version}"

  def cetToAddCmd(cet: CollectionEventType) = Json.obj(
    "studyId"              -> cet.studyId.id,
    "name"                 -> cet.name,
    "description"          -> cet.description,
    "recurring"            -> cet.recurring,
    "specimenGroupData"    -> Json.arr(
      Json.obj(
        "specimenGroupId"  -> cet.specimenGroupData(0).specimenGroupId,
        "maxCount"         -> cet.specimenGroupData(0).maxCount,
        "amount"           -> Some(cet.specimenGroupData(0).amount)
      )),
    "annotationTypeData"   -> Json.arr(
      Json.obj(
        "annotationTypeId" -> cet.annotationTypeData(0).annotationTypeId,
        "required"         -> cet.annotationTypeData(0).required
      ))
  )

  def cetToUpdateCmd(cet: CollectionEventType) =
    cetToAddCmd(cet) ++ Json.obj(
      "id"              -> cet.id.id,
      "expectedVersion" -> Some(cet.version)
    )

  def addOnNonDisabledStudy(study: Study) {
    studyRepository.put(study)

    val sg = factory.createSpecimenGroup
    specimenGroupRepository.put(sg)

    val annotType = factory.createCollectionEventAnnotationType
    collectionEventAnnotationTypeRepository.put(annotType)

    val cet = factory.createCollectionEventType.copy(
      specimenGroupData = List(factory.createCollectionEventTypeSpecimenGroupData),
      annotationTypeData = List(factory.createCollectionEventTypeAnnotationTypeData))

    val json = makeRequest(POST, uri(study), BAD_REQUEST, cetToAddCmd(cet))

    (json \ "status").as[String] must include ("error")
    (json \ "message").as[String] must include ("is not disabled")
  }

  def updateOnNonDisabledStudy(study: Study) {
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

    val json = makeRequest(PUT, uri(study, cet2), BAD_REQUEST, cetToUpdateCmd(cet2))

    (json \ "status").as[String] must include ("error")
    (json \ "message").as[String] must include ("is not disabled")
  }

  def removeOnNonDisabledStudy(study: Study) {
    studyRepository.put(study)

    val sg = factory.createSpecimenGroup
    specimenGroupRepository.put(sg)

    val annotType = factory.createCollectionEventAnnotationType
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
      "list none" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val json = makeRequest(GET, uri(study))
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size 0
      }

      "list a single collection event type" in new App(fakeApp) {
        doLogin
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

      "get a single collection event type" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val cet = factory.createCollectionEventType
        collectionEventTypeRepository.put(cet)

        val json = makeRequest(GET, uriWithQuery(study, cet))
        (json \ "status").as[String] must include ("success")
        val jsonObj = (json \ "data").as[JsObject]
        compareObj(jsonObj, cet)
      }

      "list multiple collection event types" in new App(fakeApp) {
        doLogin
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

      "fail for invalid study id" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy

        val json = makeRequest(GET, uri(study), BAD_REQUEST)
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("invalid study id")
      }

      "fail for an invalid study ID when using a collection event type id" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        val cet = factory.createCollectionEventType

        val json = makeRequest(GET, uriWithQuery(study, cet), BAD_REQUEST)
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("invalid study id")
      }

      "fail for an invalid collection event type id" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val cet = factory.createCollectionEventType

        val json = makeRequest(GET, uriWithQuery(study, cet), BAD_REQUEST)
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("collection event type does not exist")
      }

    }

    "POST /studies/cetypes" must {
      "add a collection event type" in new App(fakeApp) {
        doLogin
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
    }

    "POST /studies/cetypes" must {
      "not add a collection event type to an enabled study" in new App(fakeApp) {
        doLogin
        addOnNonDisabledStudy(
          factory.createDisabledStudy.enable(1, 1) | fail)
      }

      "not add a collection event type to an retired study" in new App(fakeApp) {
        doLogin
        addOnNonDisabledStudy(
          factory.createDisabledStudy.retire | fail)
      }
    }

    "PUT /studies/cetypes" must {
      "update a collection event type" in new App(fakeApp) {
        doLogin
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

      "not update a collection event type on an enabled study" in new App(fakeApp) {
        doLogin
        updateOnNonDisabledStudy(
          factory.createDisabledStudy.enable(1, 1) | fail)
      }

      "not update a collection event type on an retired study" in new App(fakeApp) {
        doLogin
        updateOnNonDisabledStudy(
          factory.createDisabledStudy.retire | fail)
      }
    }

    "DELETE /studies/cetypes" must {
      "remove a collection event type" in new App(fakeApp) {
        doLogin
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

      "not remove a collection event type on an enabled study" in new App(fakeApp) {
        doLogin
        removeOnNonDisabledStudy(
          factory.createDisabledStudy.enable(1, 1) | fail)
      }

      "not remove a collection event type on an retired study" in new App(fakeApp) {
        doLogin
        removeOnNonDisabledStudy(
          factory.createDisabledStudy.retire | fail)
      }
    }
  }

}
