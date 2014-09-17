package org.biobank.controllers.study

import org.biobank.controllers.BbwebPlugin
import org.biobank.domain.study.{ Study, SpecimenGroup }
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

class CeventTypeControllerSpec extends ControllerFixture {

  val log = LoggerFactory.getLogger(this.getClass)

  def addOnNonDisabledStudy(study: Study) {
    use[BbwebPlugin].studyRepository.put(study)

    val sg = factory.createSpecimenGroup
    use[BbwebPlugin].specimenGroupRepository.put(sg)

    val annotType = factory.createCollectionEventAnnotationType
    use[BbwebPlugin].collectionEventAnnotationTypeRepository.put(annotType)

    val cet = factory.createCollectionEventType.copy(
      specimenGroupData = List(factory.createCollectionEventTypeSpecimenGroupData),
      annotationTypeData = List(factory.createCollectionEventTypeAnnotationTypeData))

    val cmdJson = Json.obj(
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

    val json = makeRequest(POST, "/studies/cetypes", BAD_REQUEST, cmdJson)

    (json \ "status").as[String] should include ("error")
    (json \ "message").as[String] should include ("is not disabled")
  }

  def updateOnNonDisabledStudy(study: Study) {
    use[BbwebPlugin].studyRepository.put(study)

    val sg = factory.createSpecimenGroup
    use[BbwebPlugin].specimenGroupRepository.put(sg)

    val annotType = factory.createCollectionEventAnnotationType
    use[BbwebPlugin].collectionEventAnnotationTypeRepository.put(annotType)

    val cet = factory.createCollectionEventType
    use[BbwebPlugin].collectionEventTypeRepository.put(cet)

    val cet2 = factory.createCollectionEventType.copy(
      specimenGroupData = List(factory.createCollectionEventTypeSpecimenGroupData),
      annotationTypeData = List(factory.createCollectionEventTypeAnnotationTypeData))

    val cmdJson = Json.obj(
      "studyId"              -> cet.studyId.id,
      "id"                   -> cet.id.id,
      "expectedVersion"      -> Some(cet.version),
      "name"                 -> cet2.name,
      "description"          -> cet2.description,
      "recurring"            -> cet2.recurring,
      "specimenGroupData"    -> Json.arr(
        Json.obj(
          "specimenGroupId"  -> cet2.specimenGroupData(0).specimenGroupId,
          "maxCount"         -> cet2.specimenGroupData(0).maxCount,
          "amount"           -> Some(cet2.specimenGroupData(0).amount)
        )),
      "annotationTypeData"   -> Json.arr(
        Json.obj(
          "annotationTypeId" -> cet2.annotationTypeData(0).annotationTypeId,
          "required"         -> cet2.annotationTypeData(0).required
        ))
    )

    val json = makeRequest(PUT, s"/studies/cetypes/${cet.id.id}", BAD_REQUEST, cmdJson)

    (json \ "status").as[String] should include ("error")
    (json \ "message").as[String] should include ("is not disabled")
  }

  def removeOnNonDisabledStudy(study: Study) {
    use[BbwebPlugin].studyRepository.put(study)

    val sg = factory.createSpecimenGroup
    use[BbwebPlugin].specimenGroupRepository.put(sg)

    val annotType = factory.createCollectionEventAnnotationType
    use[BbwebPlugin].collectionEventAnnotationTypeRepository.put(annotType)

    val cet = factory.createCollectionEventType.copy(
      specimenGroupData = List(factory.createCollectionEventTypeSpecimenGroupData),
      annotationTypeData = List(factory.createCollectionEventTypeAnnotationTypeData))
    use[BbwebPlugin].collectionEventTypeRepository.put(cet)

    val json = makeRequest(
      DELETE,
      s"/studies/cetypes/${cet.studyId.id}/${cet.id.id}/${cet.version}",
      BAD_REQUEST)

    (json \ "status").as[String] should include ("error")
    (json \ "message").as[String] should include ("is not disabled")
  }

  "Collection Event Type REST API" when {

    "GET /studies/cetypes" should {
      "list none" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val json = makeRequest(GET, s"/studies/cetypes/${study.id.id}")
        (json \ "status").as[String] should include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList should have size 0
      }
    }

    "GET /studies/cetypes" should {
      "list a single collection event type" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val cet = factory.createCollectionEventType
        use[BbwebPlugin].collectionEventTypeRepository.put(cet)

        val json = makeRequest(GET, s"/studies/cetypes/${study.id.id}")
        (json \ "status").as[String] should include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList should have size 1
        compareObj(jsonList(0), cet)
      }
    }

    "GET /studies/cetypes" should {
      "get a single collection event type" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val cet = factory.createCollectionEventType
        use[BbwebPlugin].collectionEventTypeRepository.put(cet)

        val json = makeRequest(GET, s"/studies/cetypes/${study.id.id}?cetId=${cet.id.id}")
        (json \ "status").as[String] should include ("success")
        val jsonObj = (json \ "data").as[JsObject]
        compareObj(jsonObj, cet)
      }
    }

    "GET /studies/cetypes" should {
      "list multiple collection event types" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val cet1 = factory.createCollectionEventType.copy(
          specimenGroupData = List(factory.createCollectionEventTypeSpecimenGroupData),
          annotationTypeData = List(factory.createCollectionEventTypeAnnotationTypeData))

        val cet2 = factory.createCollectionEventType.copy(
          specimenGroupData = List(factory.createCollectionEventTypeSpecimenGroupData),
          annotationTypeData = List(factory.createCollectionEventTypeAnnotationTypeData))

        val cetypes = List(cet1, cet2)
        cetypes map { cet => use[BbwebPlugin].collectionEventTypeRepository.put(cet) }

        val json = makeRequest(GET, s"/studies/cetypes/${study.id.id}")
        (json \ "status").as[String] should include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]

        jsonList should have size cetypes.size
          (jsonList zip cetypes).map { item => compareObj(item._1, item._2) }
        ()
      }
    }

    "POST /studies/cetypes" should {
      "add a collection event type" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val sg = factory.createSpecimenGroup
        use[BbwebPlugin].specimenGroupRepository.put(sg)

        val annotType = factory.createCollectionEventAnnotationType
        use[BbwebPlugin].collectionEventAnnotationTypeRepository.put(annotType)

        val cet = factory.createCollectionEventType.copy(
          specimenGroupData = List(factory.createCollectionEventTypeSpecimenGroupData),
          annotationTypeData = List(factory.createCollectionEventTypeAnnotationTypeData))

        val cmdJson = Json.obj(
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

        val json = makeRequest(POST, "/studies/cetypes", json = cmdJson)

        (json \ "status").as[String] should include ("success")
      }
    }

    "POST /studies/cetypes" should {
      "not add a collection event type to an enabled study" in new WithApplication(fakeApplication()) {
        doLogin
        addOnNonDisabledStudy(
          factory.createDisabledStudy.enable(1, 1) | fail)
      }
    }

    "POST /studies/cetypes" should {
      "not add a collection event type to an retired study" in new WithApplication(fakeApplication()) {
        doLogin
        addOnNonDisabledStudy(
          factory.createDisabledStudy.retire | fail)
      }
    }

    "PUT /studies/cetypes" should {
      "update a collection event type" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val sg = factory.createSpecimenGroup
        use[BbwebPlugin].specimenGroupRepository.put(sg)

        val annotType = factory.createCollectionEventAnnotationType
        use[BbwebPlugin].collectionEventAnnotationTypeRepository.put(annotType)

        val cet = factory.createCollectionEventType
        use[BbwebPlugin].collectionEventTypeRepository.put(cet)

        val cet2 = factory.createCollectionEventType.copy(
          specimenGroupData = List(factory.createCollectionEventTypeSpecimenGroupData),
          annotationTypeData = List(factory.createCollectionEventTypeAnnotationTypeData))

        val cmdJson = Json.obj(
          "studyId"              -> cet.studyId.id,
          "id"                   -> cet.id.id,
          "expectedVersion"      -> Some(cet.version),
          "name"                 -> cet2.name,
          "description"          -> cet2.description,
          "recurring"            -> cet2.recurring,
          "specimenGroupData"    -> Json.arr(
            Json.obj(
              "specimenGroupId"  -> cet2.specimenGroupData(0).specimenGroupId,
              "maxCount"         -> cet2.specimenGroupData(0).maxCount,
              "amount"           -> Some(cet2.specimenGroupData(0).amount)
            )),
          "annotationTypeData"   -> Json.arr(
            Json.obj(
              "annotationTypeId" -> cet2.annotationTypeData(0).annotationTypeId,
              "required"         -> cet2.annotationTypeData(0).required
            ))
        )

        val json = makeRequest(PUT, s"/studies/cetypes/${cet.id.id}", json = cmdJson)

        (json \ "status").as[String] should include ("success")
      }
    }

    "PUT /studies/cetypes" should {
      "not update a collection event type on an enabled study" in new WithApplication(fakeApplication()) {
        doLogin
        updateOnNonDisabledStudy(
          factory.createDisabledStudy.enable(1, 1) | fail)
      }
    }

    "PUT /studies/cetypes" should {
      "not update a collection event type on an retired study" in new WithApplication(fakeApplication()) {
        doLogin
        updateOnNonDisabledStudy(
          factory.createDisabledStudy.retire | fail)
      }
    }

    "DELETE /studies/cetypes" should {
      "remove a collection event type" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val sg = factory.createSpecimenGroup
        use[BbwebPlugin].specimenGroupRepository.put(sg)

        val annotType = factory.createCollectionEventAnnotationType
        use[BbwebPlugin].collectionEventAnnotationTypeRepository.put(annotType)

        val cet = factory.createCollectionEventType.copy(
          specimenGroupData = List(factory.createCollectionEventTypeSpecimenGroupData),
          annotationTypeData = List(factory.createCollectionEventTypeAnnotationTypeData))
        use[BbwebPlugin].collectionEventTypeRepository.put(cet)

        val json = makeRequest(
          DELETE,
          s"/studies/cetypes/${cet.studyId.id}/${cet.id.id}/${cet.version}")

        (json \ "status").as[String] should include ("success")
      }
    }

    "DELETE /studies/cetypes" should {
      "not remove a collection event type on an enabled study" in new WithApplication(fakeApplication()) {
        doLogin
        removeOnNonDisabledStudy(
          factory.createDisabledStudy.enable(1, 1) | fail)
      }
    }

    "DELETE /studies/cetypes" should {
      "not remove a collection event type on an retired study" in new WithApplication(fakeApplication()) {
        doLogin
        removeOnNonDisabledStudy(
          factory.createDisabledStudy.retire | fail)
      }
    }
  }

}
