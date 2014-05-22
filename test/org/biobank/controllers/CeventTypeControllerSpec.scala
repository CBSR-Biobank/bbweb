package org.biobank.controllers

import org.biobank.fixture.ControllerFixture
import org.biobank.service.json.JsonHelper._

import play.api.test.Helpers._
import play.api.test.FakeApplication
import play.api.libs.json._
import org.scalatest.Tag
import org.slf4j.LoggerFactory
import org.joda.time.DateTime

class CollectionEventTypeControllerSpec extends ControllerFixture {

  val log = LoggerFactory.getLogger(this.getClass)

  describe("Collection Event Type REST API") {
    describe("GET /studies/cetypes") {
      it("should list none") {
        running(fakeApplication) {
          val appRepositories = new AppRepositories

          val study = factory.createDisabledStudy
          appRepositories.studyRepository.put(study)

          val idJson = Json.obj("id" -> study.id.id)
          val json = makeJsonRequest(GET, "/studies/cetypes", json = idJson)
          val jsonList = json.as[List[JsObject]]
          jsonList should have size 0
        }
      }
    }

    describe("GET /studies/cetypes") {
      it("should list a single collection event type") {
        running(fakeApplication) {
          val appRepositories = new AppRepositories

          val study = factory.createDisabledStudy
          appRepositories.studyRepository.put(study)

          val cet = factory.createCollectionEventType
          appRepositories.collectionEventTypeRepository.put(cet)

          val idJson = Json.obj("id" -> study.id.id)
          val json = makeJsonRequest(GET, "/studies/cetypes", json = idJson)
          val jsonList = json.as[List[JsObject]]
          jsonList should have size 1
          compareObj(jsonList(0), cet)
        }
      }
    }

    describe("GET /studies/cetypes") {
      it("should list multiple collection event types") {
        running(fakeApplication) {
          val appRepositories = new AppRepositories

          val study = factory.createDisabledStudy
          appRepositories.studyRepository.put(study)

          val cet1 = factory.createCollectionEventType.copy(
            specimenGroupData = List(factory.createCollectionEventTypeSpecimenGroupData),
            annotationTypeData = List(factory.createCollectionEventTypeAnnotationTypeData))

          val cet2 = factory.createCollectionEventType.copy(
            specimenGroupData = List(factory.createCollectionEventTypeSpecimenGroupData),
            annotationTypeData = List(factory.createCollectionEventTypeAnnotationTypeData))

          val cetypes = List(cet1, cet2)
          cetypes map { cet => appRepositories.collectionEventTypeRepository.put(cet) }

          val idJson = Json.obj("id" -> study.id.id)
          val json = makeJsonRequest(GET, "/studies/cetypes", json = idJson)
          val jsonList = json.as[List[JsObject]]

          jsonList should have size cetypes.size
          (jsonList zip cetypes).map { item => compareObj(item._1, item._2) }
        }
      }
    }

    describe("POST /studies/cetypes") {
      it("should add a collection event type", Tag("single")) {
        running(fakeApplication) {
          val appRepositories = new AppRepositories

          val study = factory.createDisabledStudy
          appRepositories.studyRepository.put(study)

          val sg = factory.createSpecimenGroup
          appRepositories.specimenGroupRepository.put(sg)

          val annotType = factory.createCollectionEventAnnotationType
          appRepositories.collectionEventAnnotationTypeRepository.put(annotType)

          val cet = factory.createCollectionEventType.copy(
            specimenGroupData = List(factory.createCollectionEventTypeSpecimenGroupData),
            annotationTypeData = List(factory.createCollectionEventTypeAnnotationTypeData))

          val cmdJson = Json.obj(
            "type"                 -> "AddCollectionEventTypeCmd",
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

          log.info(s"cmdJson: $cmdJson")

          val json = makeJsonRequest(POST, "/studies/cetypes", json = cmdJson)

          (json \ "message").as[String] should include ("collection event type added")
        }
      }
    }

    describe("PUT /studies/cetypes") {
      it("should update a collection event type") (pending)
    }

    describe("DELETE /studies/cetypes") {
      it("should remove a collection event type") (pending)
    }
  }

}
