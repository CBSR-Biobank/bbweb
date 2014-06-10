package org.biobank.controllers

import org.biobank.domain.study.{ Study, CollectionEventAnnotationType }
import org.biobank.fixture.ControllerFixture
import org.biobank.service.json.JsonHelper._

import play.api.test.Helpers._
import play.api.test.FakeApplication
import play.api.libs.json._
import org.scalatest.Tag
import org.slf4j.LoggerFactory
import org.joda.time.DateTime

class CeventAnnotTypeControllerSpec extends ControllerFixture {

  val log = LoggerFactory.getLogger(this.getClass)

  private def annotTypeToAddCmdJson(annotType: CollectionEventAnnotationType) = {
    Json.obj(
      "type"          -> "AddCollectionEventAnnotationTypeCmd",
      "studyId"       -> annotType.studyId.id,
      "name"          -> annotType.name,
      "description"   -> annotType.description,
      "valueType"     -> annotType.valueType.toString,
      "maxValueCount" -> annotType.maxValueCount,
      "options"       -> annotType.options
    )
  }

  private def annotTypeToUpdateCmdJson(annotType: CollectionEventAnnotationType) = {
    Json.obj(
      "type"            -> "UpdateCollectionEventAnnotationTypeCmd",
      "studyId"         -> annotType.studyId.id,
      "id"              -> annotType.id.id,
      "expectedVersion" -> Some(annotType.version),
      "name"            -> annotType.name,
      "valueType"       -> annotType.valueType.toString,
      "maxValueCount"   -> annotType.maxValueCount,
      "options"         -> annotType.options
    )
  }

  private def annotTypeToRemoveCmdJson(annotType: CollectionEventAnnotationType) = {
    Json.obj(
      "type"            -> "RemoveCollectionEventAnnotationTypeCmd",
      "studyId"         -> annotType.studyId.id,
      "id"              -> annotType.id.id,
      "expectedVersion" -> Some(annotType.version)
    )
  }

  private def addOnNonDisabledStudy(
    appRepositories: AppRepositories,
    study: Study) {
    appRepositories.studyRepository.put(study)

    val annotType = factory.createCollectionEventAnnotationType
    appRepositories.collectionEventAnnotationTypeRepository.put(annotType)

    val json = makeJsonRequest(
      POST,
      "/studies/ceannottype",
      BAD_REQUEST,
      annotTypeToAddCmdJson(annotType))

    (json \ "message").as[String] should include ("study is not disabled")
  }

  private def updateOnNonDisabledStudy(
    appRepositories: AppRepositories,
    study: Study) {
    appRepositories.studyRepository.put(study)

    val annotType = factory.createCollectionEventAnnotationType
    appRepositories.collectionEventAnnotationTypeRepository.put(annotType)

    val json = makeJsonRequest(
      PUT,
      s"/studies/ceannottype/${annotType.id.id}",
      BAD_REQUEST,
      annotTypeToUpdateCmdJson(annotType))

    (json \ "message").as[String] should include ("study is not disabled")
  }

  def removeOnNonDisabledStudy(
    appRepositories: AppRepositories,
    study: Study) {
    appRepositories.studyRepository.put(study)

    val sg = factory.createSpecimenGroup
    appRepositories.specimenGroupRepository.put(sg)

    val annotType = factory.createCollectionEventAnnotationType
    appRepositories.collectionEventAnnotationTypeRepository.put(annotType)

    val json = makeJsonRequest(
      DELETE,
      s"/studies/ceannottype/${annotType.id.id}",
      BAD_REQUEST,
      annotTypeToRemoveCmdJson(annotType))

    (json \ "message").as[String] should include ("study is not disabled")
  }

  // describe("Collection Event Type REST API") {
  //   describe("GET /studies/ceannottype") {
  //     it("should list none") {
  //       running(fakeApplication) {
  //         val appRepositories = new AppRepositories

  //         val study = factory.createDisabledStudy
  //         appRepositories.studyRepository.put(study)

  //         val idJson = Json.obj("id" -> study.id.id)
  //         val json = makeJsonRequest(GET, "/studies/ceannottype", json = idJson)
  //         val jsonList = json.as[List[JsObject]]
  //         jsonList should have size 0
  //       }
  //     }
  //   }

  //   describe("GET /studies/ceannottype") {
  //     it("should list a single collection event annotation type") {
  //       running(fakeApplication) {
  //         val appRepositories = new AppRepositories

  //         val study = factory.createDisabledStudy
  //         appRepositories.studyRepository.put(study)

  //         val annotType = factory.createCollectionEventAnnotationType
  //         appRepositories.collectionEventAnnotationTypeRepository.put(annotType)

  //         val idJson = Json.obj("id" -> study.id.id)
  //         val json = makeJsonRequest(GET, "/studies/ceannottype", json = idJson)
  //         val jsonList = json.as[List[JsObject]]
  //         jsonList should have size 1
  //         compareObj(jsonList(0), annotType)
  //       }
  //     }
  //   }

  //   describe("GET /studies/ceannottype") {
  //     it("should list multiple collection event annotation types") {
  //       running(fakeApplication) {
  //         val appRepositories = new AppRepositories

  //         val study = factory.createDisabledStudy
  //         appRepositories.studyRepository.put(study)

  //         val annotTypes = List(
  //           factory.createCollectionEventAnnotationType,
  //           factory.createCollectionEventAnnotationType)
  //         annotTypes map { annotType => appRepositories.collectionEventAnnotationTypeRepository.put(annotType) }

  //         val idJson = Json.obj("id" -> study.id.id)
  //         val json = makeJsonRequest(GET, "/studies/ceannottype", json = idJson)
  //         val jsonList = json.as[List[JsObject]]

  //         jsonList should have size annotTypes.size
  //           (jsonList zip annotTypes).map { item => compareObj(item._1, item._2) }
  //         ()
  //       }
  //     }
  //   }

  //   describe("POST /studies/ceannottype") {
  //     it("should add a collection event annotation type") {
  //       running(fakeApplication) {
  //         val appRepositories = new AppRepositories

  //         val study = factory.createDisabledStudy
  //         appRepositories.studyRepository.put(study)

  //         val annotType = factory.createCollectionEventAnnotationType
  //         val json = makeJsonRequest(POST, "/studies/ceannottype", json = annotTypeToAddCmdJson(annotType))
  //         (json \ "message").as[String] should include ("annotation type added")
  //       }
  //     }
  //   }

  //   describe("POST /studies/ceannottype") {
  //     it("should not add a collection event annotation type to an enabled study") {
  //       running(fakeApplication) {
  //         addOnNonDisabledStudy(
  //           new AppRepositories,
  //           factory.createDisabledStudy.enable(Some(0), DateTime.now, 1, 1) | fail)
  //       }
  //     }
  //   }

  //   describe("POST /studies/ceannottype") {
  //     it("should not add a collection event annotation type to an retired study") {
  //       running(fakeApplication) {
  //         addOnNonDisabledStudy(
  //           new AppRepositories,
  //           factory.createDisabledStudy.retire(Some(0), DateTime.now) | fail)
  //       }
  //     }
  //   }

  //   describe("PUT /studies/ceannottype") {
  //     it("should update a collection event annotation type") {
  //       running(fakeApplication) {
  //         val appRepositories = new AppRepositories

  //         val study = factory.createDisabledStudy
  //         appRepositories.studyRepository.put(study)

  //         val annotType = factory.createCollectionEventAnnotationType
  //         appRepositories.collectionEventAnnotationTypeRepository.put(annotType)

  //         val annotType2 = factory.createCollectionEventAnnotationType.copy(
  //           id = annotType.id,
  //           version = annotType.version
  //         )

  //         val json = makeJsonRequest(PUT,
  //           s"/studies/ceannottype/${annotType.id.id}",
  //           json = annotTypeToUpdateCmdJson(annotType2))

  //         (json \ "message").as[String] should include ("annotation type updated")
  //       }
  //     }
  //   }

  //   describe("PUT /studies/ceannottype") {
  //     it("should not update a collection event annotation type on an enabled study") {
  //       running(fakeApplication) {
  //         updateOnNonDisabledStudy(
  //           new AppRepositories,
  //           factory.createDisabledStudy.enable(Some(0), DateTime.now, 1, 1) | fail)
  //       }
  //     }
  //   }

  //   describe("PUT /studies/ceannottype") {
  //     it("should not update a collection event annotation type on an retired study") {
  //       running(fakeApplication) {
  //         updateOnNonDisabledStudy(
  //           new AppRepositories,
  //           factory.createDisabledStudy.retire(Some(0), DateTime.now) | fail)
  //       }
  //     }
  //   }

  //   describe("DELETE /studies/ceannottype") {
  //     it("should remove a collection event annotation type") {
  //       running(fakeApplication) {
  //         val appRepositories = new AppRepositories

  //         val study = factory.createDisabledStudy
  //         appRepositories.studyRepository.put(study)

  //         val annotType = factory.createCollectionEventAnnotationType
  //         appRepositories.collectionEventAnnotationTypeRepository.put(annotType)

  //         val json = makeJsonRequest(
  //           DELETE,
  //           s"/studies/ceannottype/${annotType.id.id}",
  //           json = annotTypeToRemoveCmdJson(annotType))

  //         (json \ "message").as[String] should include ("annotation type removed")
  //       }
  //     }
  //   }

  //   describe("DELETE /studies/ceannottype") {
  //     it("should not remove a collection event annotation type on an enabled study") {
  //       running(fakeApplication) {
  //         removeOnNonDisabledStudy(
  //           new AppRepositories,
  //           factory.createDisabledStudy.enable(Some(0), DateTime.now, 1, 1) | fail)
  //       }
  //     }
  //   }

  //   describe("DELETE /studies/ceannottype") {
  //     it("should not remove a collection event annotation type on an retired study") {
  //       running(fakeApplication) {
  //         removeOnNonDisabledStudy(
  //           new AppRepositories,
  //           factory.createDisabledStudy.retire(Some(0), DateTime.now) | fail)
  //       }
  //     }
  //   }
  // }

}
