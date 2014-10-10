package org.biobank.controllers.study

import org.biobank.fixture._
import org.biobank.controllers.BbwebPlugin
import org.biobank.domain.study.{ Study, ParticipantAnnotationType }
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

class ParticipantAnnotTypeControllerSpec extends ControllerFixture {

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

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
      "/studies/pannottypes",
      BAD_REQUEST,
      annotTypeToAddCmdJson(annotType))

    (json \ "status").as[String] should include ("error")
    (json \ "message").as[String] should include ("is not disabled")
  }

  private def updateOnNonDisabledStudy(study: Study) {
    use[BbwebPlugin].studyRepository.put(study)

    val annotType = factory.createParticipantAnnotationType
    use[BbwebPlugin].participantAnnotationTypeRepository.put(annotType)

    val json = makeRequest(
      PUT,
      s"/studies/pannottypes/${annotType.id.id}",
      BAD_REQUEST,
      annotTypeToUpdateCmdJson(annotType))

    (json \ "status").as[String] should include ("error")
    (json \ "message").as[String] should include ("is not disabled")
  }

  def removeOnNonDisabledStudy(study: Study) {
    use[BbwebPlugin].studyRepository.put(study)

    val sg = factory.createSpecimenGroup
    use[BbwebPlugin].specimenGroupRepository.put(sg)

    val annotType = factory.createParticipantAnnotationType
    use[BbwebPlugin].participantAnnotationTypeRepository.put(annotType)

    val json = makeRequest(
      DELETE,
      s"/studies/pannottypes/${annotType.studyId.id}/${annotType.id.id}/${annotType.version}",
      BAD_REQUEST)

    (json \ "status").as[String] should include ("error")
    (json \ "message").as[String] should include ("is not disabled")
  }

  "Participant Type REST API" when {

    "GET /studies/pannottypes" should {
      "list none" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val json = makeRequest(GET, s"/studies/pannottypes/${study.id.id}")
        (json \ "status").as[String] should include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList should have size 0
      }

      "list a single participant annotation type" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val annotType = factory.createParticipantAnnotationType
        use[BbwebPlugin].participantAnnotationTypeRepository.put(annotType)

        val json = makeRequest(GET, s"/studies/pannottypes/${study.id.id}")
        (json \ "status").as[String] should include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList should have size 1
        compareObj(jsonList(0), annotType)
      }

      "list multiple participant annotation types" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val annotTypes = List(
          factory.createParticipantAnnotationType,
          factory.createParticipantAnnotationType)
        annotTypes map { annotType => use[BbwebPlugin].participantAnnotationTypeRepository.put(annotType) }

        val json = makeRequest(GET, s"/studies/pannottypes/${study.id.id}")
        (json \ "status").as[String] should include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]

        jsonList should have size annotTypes.size
          (jsonList zip annotTypes).map { item => compareObj(item._1, item._2) }
        ()
      }

      "fail for an invalid study ID" in new WithApplication(fakeApplication()) {
        doLogin
        val studyId = nameGenerator.next[Study]

        val json = makeRequest(GET, s"/studies/pannottypes/$studyId", BAD_REQUEST)
        (json \ "status").as[String] should include ("error")
        (json \ "message").as[String] should include ("invalid study id")
      }

      "fail for an invalid study ID when using an annotation type id" taggedAs(Tag("1")) in new WithApplication(fakeApplication()) {
        doLogin
        val studyId = nameGenerator.next[Study]
        val annotTypeId = nameGenerator.next[ParticipantAnnotationType]

        val json = makeRequest(GET, s"/studies/pannottypes/$studyId?annotTypeId=$annotTypeId", BAD_REQUEST)
        (json \ "status").as[String] should include ("error")
        (json \ "message").as[String] should include ("invalid study id")
      }

      "fail for an invalid participant annotation type id" taggedAs(Tag("1")) in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val annotTypeId = nameGenerator.next[ParticipantAnnotationType]

        val json = makeRequest(GET, s"/studies/pannottypes/${study.id}?annotTypeId=$annotTypeId", BAD_REQUEST)
        (json \ "status").as[String] should include ("error")
        (json \ "message").as[String] should include ("annotation type does not exist")
      }

    }

    "POST /studies/pannottypes" should {
      "add a participant annotation type" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val annotType = factory.createParticipantAnnotationType
        val json = makeRequest(POST, "/studies/pannottypes", json = annotTypeToAddCmdJson(annotType))
          (json \ "status").as[String] should include ("success")
      }
    }

    "POST /studies/pannottypes" should {
      "not add a participant annotation type to an enabled study" in new WithApplication(fakeApplication()) {
        doLogin
        addOnNonDisabledStudy(
          factory.createDisabledStudy.enable(1, 1) | fail)
      }
    }

    "POST /studies/pannottypes" should {
      "not add a participant annotation type to an retired study" in new WithApplication(fakeApplication()) {
        doLogin
        addOnNonDisabledStudy(
          factory.createDisabledStudy.retire | fail)
      }
    }

    "PUT /studies/pannottypes" should {
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
          s"/studies/pannottypes/${annotType.id.id}",
          json = annotTypeToUpdateCmdJson(annotType2))

        (json \ "status").as[String] should include ("success")
      }
    }

    "PUT /studies/pannottypes" should {
      "not update a participant annotation type on an enabled study" in new WithApplication(fakeApplication()) {
        doLogin
        updateOnNonDisabledStudy(
          factory.createDisabledStudy.enable(1, 1) | fail)
      }
    }

    "PUT /studies/pannottypes" should {
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
          s"/studies/pannottypes/${annotType.studyId.id}/${annotType.id.id}/${annotType.version}")

        (json \ "status").as[String] should include ("success")
      }
    }

    "DELETE /studies/pannottypes" should {
      "not remove a participant annotation type on an enabled study" in new WithApplication(fakeApplication()) {
        doLogin
        removeOnNonDisabledStudy(
          factory.createDisabledStudy.enable(1, 1) | fail)
      }
    }
  }

}
