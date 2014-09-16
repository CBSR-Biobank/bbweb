package org.biobank.controllers

import org.biobank.domain.study.{ Study, ParticipantAnnotationType }
import org.biobank.fixture.ControllerFixture
import org.biobank.service.json.JsonHelper._

import play.api.test.Helpers._
import play.api.test.WithApplication
import play.api.libs.json._
import org.scalatest.Tag
import org.slf4j.LoggerFactory
import org.joda.time.DateTime
import com.typesafe.plugin._
import play.api.Play.current

class ParticipantAnnotTypeControllerSpec extends ControllerFixture {

  val log = LoggerFactory.getLogger(this.getClass)

  private def annotTypeToAddCmdJson(annotType: ParticipantAnnotationType) = {
    Json.obj(
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

  private def addOnNonDisabledStudy(study: Study) {
    use[BbwebPlugin].studyRepository.put(study)

    val annotType = factory.createParticipantAnnotationType
    use[BbwebPlugin].participantAnnotationTypeRepository.put(annotType)

    val json = makeRequest(
      POST,
      "/studies/pannottype",
      BAD_REQUEST,
      annotTypeToAddCmdJson(annotType))

    (json \ "status").as[String] should include ("error")
    (json \ "message").as[String] should include ("study is not disabled")
  }

  private def updateOnNonDisabledStudy(study: Study) {
    use[BbwebPlugin].studyRepository.put(study)

    val annotType = factory.createParticipantAnnotationType
    use[BbwebPlugin].participantAnnotationTypeRepository.put(annotType)

    val json = makeRequest(
      PUT,
      s"/studies/pannottype/${annotType.id.id}",
      BAD_REQUEST,
      annotTypeToUpdateCmdJson(annotType))

    (json \ "status").as[String] should include ("error")
    (json \ "message").as[String] should include ("study is not disabled")
  }

  def removeOnNonDisabledStudy(study: Study) {
    use[BbwebPlugin].studyRepository.put(study)

    val sg = factory.createSpecimenGroup
    use[BbwebPlugin].specimenGroupRepository.put(sg)

    val annotType = factory.createParticipantAnnotationType
    use[BbwebPlugin].participantAnnotationTypeRepository.put(annotType)

    val json = makeRequest(
      DELETE,
      s"/studies/pannottype/${annotType.studyId.id}/${annotType.id.id}/${annotType.version}",
      BAD_REQUEST)

    (json \ "status").as[String] should include ("error")
    (json \ "message").as[String] should include ("study is not disabled")
  }

  "Participant Type REST API" when {

    "GET /studies/pannottype" should {
      "list none" taggedAs(Tag("single")) in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val json = makeRequest(GET, s"/studies/pannottype/${study.id.id}")
        (json \ "status").as[String] should include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList should have size 0
      }
    }

    "GET /studies/pannottype" should {
      "list a single participant annotation type" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val annotType = factory.createParticipantAnnotationType
        use[BbwebPlugin].participantAnnotationTypeRepository.put(annotType)

        val json = makeRequest(GET, s"/studies/pannottype/${study.id.id}")
        (json \ "status").as[String] should include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList should have size 1
        compareObj(jsonList(0), annotType)
      }
    }

    "GET /studies/pannottype" should {
      "list multiple participant annotation types" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val annotTypes = List(
          factory.createParticipantAnnotationType,
          factory.createParticipantAnnotationType)
        annotTypes map { annotType => use[BbwebPlugin].participantAnnotationTypeRepository.put(annotType) }

        val json = makeRequest(GET, s"/studies/pannottype/${study.id.id}")
        (json \ "status").as[String] should include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]

        jsonList should have size annotTypes.size
          (jsonList zip annotTypes).map { item => compareObj(item._1, item._2) }
        ()
      }
    }

    "POST /studies/pannottype" should {
      "add a participant annotation type" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val annotType = factory.createParticipantAnnotationType
        val json = makeRequest(POST, "/studies/pannottype", json = annotTypeToAddCmdJson(annotType))
          (json \ "status").as[String] should include ("success")
      }
    }

    "POST /studies/pannottype" should {
      "not add a participant annotation type to an enabled study" in new WithApplication(fakeApplication()) {
        doLogin
        addOnNonDisabledStudy(
          factory.createDisabledStudy.enable(1, 1) | fail)
      }
    }

    "POST /studies/pannottype" should {
      "not add a participant annotation type to an retired study" in new WithApplication(fakeApplication()) {
        doLogin
        addOnNonDisabledStudy(
          factory.createDisabledStudy.retire | fail)
      }
    }

    "PUT /studies/pannottype" should {
      "update a participant annotation type" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val annotType = factory.createParticipantAnnotationType
        use[BbwebPlugin].participantAnnotationTypeRepository.put(annotType)

        val annotType2 = factory.createParticipantAnnotationType.copy(
          id = annotType.id,
          version = annotType.version
        )

        val json = makeRequest(PUT,
          s"/studies/pannottype/${annotType.id.id}",
          json = annotTypeToUpdateCmdJson(annotType2))

        (json \ "status").as[String] should include ("success")
      }
    }

    "PUT /studies/pannottype" should {
      "not update a participant annotation type on an enabled study" in new WithApplication(fakeApplication()) {
        doLogin
        updateOnNonDisabledStudy(
          factory.createDisabledStudy.enable(1, 1) | fail)
      }
    }

    "PUT /studies/pannottype" should {
      "not update a participant annotation type on an retired study" in new WithApplication(fakeApplication()) {
        doLogin
        updateOnNonDisabledStudy(
          factory.createDisabledStudy.retire | fail)
      }
    }

    "DELETE /studies/pannottype" should {
      "remove a participant annotation type" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val annotType = factory.createParticipantAnnotationType
        use[BbwebPlugin].participantAnnotationTypeRepository.put(annotType)

        val json = makeRequest(
          DELETE,
          s"/studies/pannottype/${annotType.studyId.id}/${annotType.id.id}/${annotType.version}")

        (json \ "status").as[String] should include ("success")
      }
    }

    "DELETE /studies/pannottype" should {
      "not remove a participant annotation type on an enabled study" in new WithApplication(fakeApplication()) {
        doLogin
        removeOnNonDisabledStudy(
          factory.createDisabledStudy.enable(1, 1) | fail)
      }
    }
  }

}
