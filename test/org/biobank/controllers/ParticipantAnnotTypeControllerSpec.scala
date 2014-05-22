package org.biobank.controllers

import org.biobank.domain.study.{ Study, ParticipantAnnotationType }
import org.biobank.fixture.ControllerFixture
import org.biobank.service.json.JsonHelper._

import play.api.test.Helpers._
import play.api.test.FakeApplication
import play.api.libs.json._
import org.scalatest.Tag
import org.slf4j.LoggerFactory
import org.joda.time.DateTime

class ParticipantAnnotTypeControllerSpec extends ControllerFixture {

  val log = LoggerFactory.getLogger(this.getClass)

  private def annotTypeToAddCmdJson(annotType: ParticipantAnnotationType) = {
    Json.obj(
      "type"          -> "AddParticipantAnnotationTypeCmd",
      "studyId"       -> annotType.studyId.id,
      "name"          -> annotType.name,
      "description"   -> annotType.description,
      "valueType"     -> annotType.valueType.toString,
      "maxValueCount" -> annotType.maxValueCount,
      "options"       -> annotType.options,
      "required"      -> annotType.required
    )
  }

  private def annotTypeToUpdateCmdJson(annotType: ParticipantAnnotationType) = {
    Json.obj(
      "type"            -> "UpdateParticipantAnnotationTypeCmd",
      "studyId"         -> annotType.studyId.id,
      "id"              -> annotType.id.id,
      "expectedVersion" -> Some(annotType.version),
      "name"            -> annotType.name,
      "valueType"       -> annotType.valueType.toString,
      "maxValueCount"   -> annotType.maxValueCount,
      "options"         -> annotType.options,
      "required"        -> annotType.required
    )
  }

  private def annotTypeToRemoveCmdJson(annotType: ParticipantAnnotationType) = {
    Json.obj(
      "type"            -> "RemoveParticipantAnnotationTypeCmd",
      "studyId"         -> annotType.studyId.id,
      "id"              -> annotType.id.id,
      "expectedVersion" -> Some(annotType.version)
    )
  }

  private def addOnNonDisabledStudy(
    appRepositories: AppRepositories,
    study: Study) {
    appRepositories.studyRepository.put(study)

    val annotType = factory.createParticipantAnnotationType
    appRepositories.participantAnnotationTypeRepository.put(annotType)

    val json = makeJsonRequest(
      POST,
      "/studies/pannottype",
      BAD_REQUEST,
      annotTypeToAddCmdJson(annotType))

    (json \ "message").as[String] should include ("study is not disabled")
  }

  private def updateOnNonDisabledStudy(
    appRepositories: AppRepositories,
    study: Study) {
    appRepositories.studyRepository.put(study)

    val annotType = factory.createParticipantAnnotationType
    appRepositories.participantAnnotationTypeRepository.put(annotType)

    val json = makeJsonRequest(
      PUT,
      s"/studies/pannottype/${annotType.id.id}",
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

    val annotType = factory.createParticipantAnnotationType
    appRepositories.participantAnnotationTypeRepository.put(annotType)

    val json = makeJsonRequest(
      DELETE,
      s"/studies/pannottype/${annotType.id.id}",
      BAD_REQUEST,
      annotTypeToRemoveCmdJson(annotType))

    (json \ "message").as[String] should include ("study is not disabled")
  }

  describe("Participant Type REST API") {
    describe("GET /studies/pannottype") {
      it("should list none") {
        running(fakeApplication) {
          val appRepositories = new AppRepositories

          val study = factory.createDisabledStudy
          appRepositories.studyRepository.put(study)

          val idJson = Json.obj("id" -> study.id.id)
          val json = makeJsonRequest(GET, "/studies/pannottype", json = idJson)
          val jsonList = json.as[List[JsObject]]
          jsonList should have size 0
        }
      }
    }

    describe("GET /studies/pannottype") {
      it("should list a single participant annotation type") {
        running(fakeApplication) {
          val appRepositories = new AppRepositories

          val study = factory.createDisabledStudy
          appRepositories.studyRepository.put(study)

          val annotType = factory.createParticipantAnnotationType
          appRepositories.participantAnnotationTypeRepository.put(annotType)

          val idJson = Json.obj("id" -> study.id.id)
          val json = makeJsonRequest(GET, "/studies/pannottype", json = idJson)
          val jsonList = json.as[List[JsObject]]
          jsonList should have size 1
          compareObj(jsonList(0), annotType)
        }
      }
    }

    describe("GET /studies/pannottype") {
      it("should list multiple participant annotation types") {
        running(fakeApplication) {
          val appRepositories = new AppRepositories

          val study = factory.createDisabledStudy
          appRepositories.studyRepository.put(study)

          val annotTypes = List(
            factory.createParticipantAnnotationType,
            factory.createParticipantAnnotationType)
          annotTypes map { annotType => appRepositories.participantAnnotationTypeRepository.put(annotType) }

          val idJson = Json.obj("id" -> study.id.id)
          val json = makeJsonRequest(GET, "/studies/pannottype", json = idJson)
          val jsonList = json.as[List[JsObject]]

          jsonList should have size annotTypes.size
            (jsonList zip annotTypes).map { item => compareObj(item._1, item._2) }
        }
      }
    }

    describe("POST /studies/pannottype") {
      it("should add a participant annotation type") {
        running(fakeApplication) {
          val appRepositories = new AppRepositories

          val study = factory.createDisabledStudy
          appRepositories.studyRepository.put(study)

          val annotType = factory.createParticipantAnnotationType
          val json = makeJsonRequest(POST, "/studies/pannottype", json = annotTypeToAddCmdJson(annotType))
          (json \ "message").as[String] should include ("annotation type added")
        }
      }
    }

    describe("POST /studies/pannottype") {
      it("should not add a participant annotation type to an enabled study") {
        running(fakeApplication) {
          addOnNonDisabledStudy(
            new AppRepositories,
            factory.createDisabledStudy.enable(Some(0), DateTime.now, 1, 1) | fail)
        }
      }
    }

    describe("POST /studies/pannottype") {
      it("should not add a participant annotation type to an retired study") {
        running(fakeApplication) {
          addOnNonDisabledStudy(
            new AppRepositories,
            factory.createDisabledStudy.retire(Some(0), DateTime.now) | fail)
        }
      }
    }

    describe("PUT /studies/pannottype") {
      it("should update a participant annotation type") {
        running(fakeApplication) {
          val appRepositories = new AppRepositories

          val study = factory.createDisabledStudy
          appRepositories.studyRepository.put(study)

          val annotType = factory.createParticipantAnnotationType
          appRepositories.participantAnnotationTypeRepository.put(annotType)

          val annotType2 = factory.createParticipantAnnotationType.copy(
            id = annotType.id,
            version = annotType.version
          )

          val json = makeJsonRequest(PUT,
            s"/studies/pannottype/${annotType.id.id}",
            json = annotTypeToUpdateCmdJson(annotType2))

          (json \ "message").as[String] should include ("annotation type updated")
        }
      }
    }

    describe("PUT /studies/pannottype") {
      it("should not update a participant annotation type on an enabled study") {
        running(fakeApplication) {
          updateOnNonDisabledStudy(
            new AppRepositories,
            factory.createDisabledStudy.enable(Some(0), DateTime.now, 1, 1) | fail)
        }
      }
    }

    describe("PUT /studies/pannottype") {
      it("should not update a participant annotation type on an retired study") {
        running(fakeApplication) {
          updateOnNonDisabledStudy(
            new AppRepositories,
            factory.createDisabledStudy.retire(Some(0), DateTime.now) | fail)
        }
      }
    }

    describe("DELETE /studies/pannottype") {
      it("should remove a participant annotation type") {
        running(fakeApplication) {
          val appRepositories = new AppRepositories

          val study = factory.createDisabledStudy
          appRepositories.studyRepository.put(study)

          val annotType = factory.createParticipantAnnotationType
          appRepositories.participantAnnotationTypeRepository.put(annotType)

          val json = makeJsonRequest(
            DELETE,
            s"/studies/pannottype/${annotType.id.id}",
            json = annotTypeToRemoveCmdJson(annotType))

          (json \ "message").as[String] should include ("annotation type removed")
        }
      }
    }

    describe("DELETE /studies/pannottype") {
      it("should not remove a participant annotation type on an enabled study") {
        running(fakeApplication) {
          removeOnNonDisabledStudy(
            new AppRepositories,
            factory.createDisabledStudy.enable(Some(0), DateTime.now, 1, 1) | fail)
        }
      }
    }

    describe("DELETE /studies/pannottype") {
      it("should not remove a participant annotation type on an retired study") {
        running(fakeApplication) {
          removeOnNonDisabledStudy(
            new AppRepositories,
            factory.createDisabledStudy.retire(Some(0), DateTime.now) | fail)
        }
      }
    }
  }

}
