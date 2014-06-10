package org.biobank.controllers

import org.biobank.domain.study.{ Study, SpecimenLinkAnnotationType }
import org.biobank.fixture.ControllerFixture
import org.biobank.service.json.JsonHelper._

import play.api.test.Helpers._
import play.api.test.FakeApplication
import play.api.libs.json._
import org.scalatest.Tag
import org.slf4j.LoggerFactory
import org.joda.time.DateTime

class SpecimenLinkAnnotTypeControllerSpec extends ControllerFixture {

  val log = LoggerFactory.getLogger(this.getClass)

  private def annotTypeToAddCmdJson(annotType: SpecimenLinkAnnotationType) = {
    Json.obj(
      "type"          -> "AddSpecimenLinkAnnotationTypeCmd",
      "studyId"       -> annotType.studyId.id,
      "name"          -> annotType.name,
      "description"   -> annotType.description,
      "valueType"     -> annotType.valueType.toString,
      "maxValueCount" -> annotType.maxValueCount,
      "options"       -> annotType.options
    )
  }

  private def annotTypeToUpdateCmdJson(annotType: SpecimenLinkAnnotationType) = {
    Json.obj(
      "type"            -> "UpdateSpecimenLinkAnnotationTypeCmd",
      "studyId"         -> annotType.studyId.id,
      "id"              -> annotType.id.id,
      "expectedVersion" -> Some(annotType.version),
      "name"            -> annotType.name,
      "valueType"       -> annotType.valueType.toString,
      "maxValueCount"   -> annotType.maxValueCount,
      "options"         -> annotType.options
    )
  }

  private def annotTypeToRemoveCmdJson(annotType: SpecimenLinkAnnotationType) = {
    Json.obj(
      "type"            -> "RemoveSpecimenLinkAnnotationTypeCmd",
      "studyId"         -> annotType.studyId.id,
      "id"              -> annotType.id.id,
      "expectedVersion" -> Some(annotType.version)
    )
  }

  private def addOnNonDisabledStudy(
    appRepositories: AppRepositories,
    study: Study) {
    appRepositories.studyRepository.put(study)

    val annotType = factory.createSpecimenLinkAnnotationType
    appRepositories.specimenLinkAnnotationTypeRepository.put(annotType)

    val json = makeJsonRequest(
      POST,
      "/studies/slannottype",
      BAD_REQUEST,
      annotTypeToAddCmdJson(annotType))

    (json \ "message").as[String] should include ("study is not disabled")
  }

  private def updateOnNonDisabledStudy(
    appRepositories: AppRepositories,
    study: Study) {
    appRepositories.studyRepository.put(study)

    val annotType = factory.createSpecimenLinkAnnotationType
    appRepositories.specimenLinkAnnotationTypeRepository.put(annotType)

    val json = makeJsonRequest(
      PUT,
      s"/studies/slannottype/${annotType.id.id}",
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

    val annotType = factory.createSpecimenLinkAnnotationType
    appRepositories.specimenLinkAnnotationTypeRepository.put(annotType)

    val json = makeJsonRequest(
      DELETE,
      s"/studies/slannottype/${annotType.id.id}",
      BAD_REQUEST,
      annotTypeToRemoveCmdJson(annotType))

    (json \ "message").as[String] should include ("study is not disabled")
  }

  // describe("Collection Event Type REST API") {
  //   describe("GET /studies/slannottype") {
  //     it("should list none") {
  //       running(fakeApplication) {
  //         val appRepositories = new AppRepositories

  //         val study = factory.createDisabledStudy
  //         appRepositories.studyRepository.put(study)

  //         val idJson = Json.obj("id" -> study.id.id)
  //         val json = makeJsonRequest(GET, "/studies/slannottype", json = idJson)
  //         val jsonList = json.as[List[JsObject]]
  //         jsonList should have size 0
  //       }
  //     }
  //   }

  //   describe("GET /studies/slannottype") {
  //     it("should list a single collection event annotation type") {
  //       running(fakeApplication) {
  //         val appRepositories = new AppRepositories

  //         val study = factory.createDisabledStudy
  //         appRepositories.studyRepository.put(study)

  //         val annotType = factory.createSpecimenLinkAnnotationType
  //         appRepositories.specimenLinkAnnotationTypeRepository.put(annotType)

  //         val idJson = Json.obj("id" -> study.id.id)
  //         val json = makeJsonRequest(GET, "/studies/slannottype", json = idJson)
  //         val jsonList = json.as[List[JsObject]]
  //         jsonList should have size 1
  //         compareObj(jsonList(0), annotType)
  //       }
  //     }
  //   }

  //   describe("GET /studies/slannottype") {
  //     it("should list multiple collection event annotation types") {
  //       running(fakeApplication) {
  //         val appRepositories = new AppRepositories

  //         val study = factory.createDisabledStudy
  //         appRepositories.studyRepository.put(study)

  //         val annotTypes = List(
  //           factory.createSpecimenLinkAnnotationType,
  //           factory.createSpecimenLinkAnnotationType)
  //         annotTypes map { annotType => appRepositories.specimenLinkAnnotationTypeRepository.put(annotType) }

  //         val idJson = Json.obj("id" -> study.id.id)
  //         val json = makeJsonRequest(GET, "/studies/slannottype", json = idJson)
  //         val jsonList = json.as[List[JsObject]]

  //         jsonList should have size annotTypes.size
  //           (jsonList zip annotTypes).map { item => compareObj(item._1, item._2) }
  //         ()
  //       }
  //     }
  //   }

  //   describe("POST /studies/slannottype") {
  //     it("should add a collection event annotation type") {
  //       running(fakeApplication) {
  //         val appRepositories = new AppRepositories

  //         val study = factory.createDisabledStudy
  //         appRepositories.studyRepository.put(study)

  //         val annotType = factory.createSpecimenLinkAnnotationType
  //         val json = makeJsonRequest(POST, "/studies/slannottype", json = annotTypeToAddCmdJson(annotType))
  //         (json \ "message").as[String] should include ("annotation type added")
  //       }
  //     }
  //   }

  //   describe("POST /studies/slannottype") {
  //     it("should not add a collection event annotation type to an enabled study") {
  //       running(fakeApplication) {
  //         addOnNonDisabledStudy(
  //           new AppRepositories,
  //           factory.createDisabledStudy.enable(Some(0), DateTime.now, 1, 1) | fail)
  //       }
  //     }
  //   }

  //   describe("POST /studies/slannottype") {
  //     it("should not add a collection event annotation type to an retired study") {
  //       running(fakeApplication) {
  //         addOnNonDisabledStudy(
  //           new AppRepositories,
  //           factory.createDisabledStudy.retire(Some(0), DateTime.now) | fail)
  //       }
  //     }
  //   }

  //   describe("PUT /studies/slannottype") {
  //     it("should update a collection event annotation type") {
  //       running(fakeApplication) {
  //         val appRepositories = new AppRepositories

  //         val study = factory.createDisabledStudy
  //         appRepositories.studyRepository.put(study)

  //         val annotType = factory.createSpecimenLinkAnnotationType
  //         appRepositories.specimenLinkAnnotationTypeRepository.put(annotType)

  //         val annotType2 = factory.createSpecimenLinkAnnotationType.copy(
  //           id = annotType.id,
  //           version = annotType.version
  //         )

  //         val json = makeJsonRequest(PUT,
  //           s"/studies/slannottype/${annotType.id.id}",
  //           json = annotTypeToUpdateCmdJson(annotType2))

  //         (json \ "message").as[String] should include ("annotation type updated")
  //       }
  //     }
  //   }

  //   describe("PUT /studies/slannottype") {
  //     it("should not update a collection event annotation type on an enabled study") {
  //       running(fakeApplication) {
  //         updateOnNonDisabledStudy(
  //           new AppRepositories,
  //           factory.createDisabledStudy.enable(Some(0), DateTime.now, 1, 1) | fail)
  //       }
  //     }
  //   }

  //   describe("PUT /studies/slannottype") {
  //     it("should not update a collection event annotation type on an retired study") {
  //       running(fakeApplication) {
  //         updateOnNonDisabledStudy(
  //           new AppRepositories,
  //           factory.createDisabledStudy.retire(Some(0), DateTime.now) | fail)
  //       }
  //     }
  //   }

  //   describe("DELETE /studies/slannottype") {
  //     it("should remove a collection event annotation type") {
  //       running(fakeApplication) {
  //         val appRepositories = new AppRepositories

  //         val study = factory.createDisabledStudy
  //         appRepositories.studyRepository.put(study)

  //         val annotType = factory.createSpecimenLinkAnnotationType
  //         appRepositories.specimenLinkAnnotationTypeRepository.put(annotType)

  //         val json = makeJsonRequest(
  //           DELETE,
  //           s"/studies/slannottype/${annotType.id.id}",
  //           json = annotTypeToRemoveCmdJson(annotType))

  //         (json \ "message").as[String] should include ("annotation type removed")
  //       }
  //     }
  //   }

  //   describe("DELETE /studies/slannottype") {
  //     it("should not remove a collection event annotation type on an enabled study") {
  //       running(fakeApplication) {
  //         removeOnNonDisabledStudy(
  //           new AppRepositories,
  //           factory.createDisabledStudy.enable(Some(0), DateTime.now, 1, 1) | fail)
  //       }
  //     }
  //   }

  //   describe("DELETE /studies/slannottype") {
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
