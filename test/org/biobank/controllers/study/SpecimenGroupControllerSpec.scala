package org.biobank.controllers.study

import org.biobank.fixture._
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
import org.scalatestplus.play._

class SpecimenGroupControllerSpec extends ControllerFixture {
  import TestGlobal._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  def uri(study: Study): String = s"/studies/${study.id.id}/sgroups"

  def uri(study: Study, specimenGroup: SpecimenGroup): String =
    uri(study) + s"/${specimenGroup.id.id}"

  def uriWithQuery(study: Study, specimenGroup: SpecimenGroup): String =
    uri(study) + s"?sgId=${specimenGroup.id.id}"

  def uri(study: Study, specimenGroup: SpecimenGroup, version: Long): String =
    uri(study, specimenGroup) + s"/${version}"

  def specimenGroupToAddCmd(sg: SpecimenGroup) = Json.obj(
    "studyId"                     -> sg.studyId.id,
    "name"                        -> sg.name,
    "description"                 -> sg.description,
    "units"                       -> sg.units,
    "anatomicalSourceType"        -> sg.anatomicalSourceType.toString,
    "preservationType"            -> sg.preservationType.toString,
    "preservationTemperatureType" -> sg.preservationTemperatureType.toString,
    "specimenType"                -> sg.specimenType.toString
  )

  def specimenGroupToUpdateCmd(sg: SpecimenGroup) = {
    specimenGroupToAddCmd(sg) ++ Json.obj(
      "id"              -> sg.id.id,
      "expectedVersion" -> Some(sg.version)
    )
  }

  def addToNonDisabledStudy(study: Study, sg: SpecimenGroup) = {
    studyRepository.put(study)
    val sg2 = sg.copy(studyId = study.id);
    val cmdJson = specimenGroupToAddCmd(sg2);
    val json = makeRequest(POST, uri(study), BAD_REQUEST, cmdJson)

    (json \ "status").as[String] must include ("error")
    (json \ "message").as[String] must include ("is not disabled")
  }

  def updateOnNonDisabledStudy(study: Study, sg: SpecimenGroup) = {
    studyRepository.put(study)
    specimenGroupRepository.put(sg)

    val sg2 = factory.createSpecimenGroup.copy(id = sg.id, studyId = study.id)
    val cmdJson = specimenGroupToUpdateCmd(sg2);
    val json = makeRequest(PUT, uri(study, sg2), BAD_REQUEST, cmdJson)

    (json \ "status").as[String] must include ("error")
    (json \ "message").as[String] must include ("is not disabled")
  }

  def removeOnNonDisabledStudy(
    study: Study,
    sg: SpecimenGroup) = {
    studyRepository.put(study)
    specimenGroupRepository.put(sg)

    val json = makeRequest(DELETE, uri(study, sg, sg.version), BAD_REQUEST)

    (json \ "status").as[String] must include ("error")
    (json \ "message").as[String] must include ("is not disabled")
  }

  "Specimen Group REST API" when {

    "GET /studies/sgroups" must {
      "list none" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val json = makeRequest(GET, uri(study))
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size 0
      }

      "list a single specimen group" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val sg = factory.createSpecimenGroup
        specimenGroupRepository.put(sg)

        val json = makeRequest(GET, uri(study))
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size 1
        compareObj(jsonList(0), sg)
      }

      "get a single specimen group" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val sg = factory.createSpecimenGroup
        specimenGroupRepository.put(sg)

        val json = makeRequest(GET, uriWithQuery(study, sg)).as[JsObject]
        (json \ "status").as[String] must include ("success")
        val jsonObj = (json \ "data")
        compareObj(jsonObj, sg)
      }

      "list multiple specimen groups" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val sgroups = List(factory.createSpecimenGroup, factory.createSpecimenGroup)
        sgroups map { sg => specimenGroupRepository.put(sg) }

        val json = makeRequest(GET, uri(study))
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size sgroups.size
          (jsonList zip sgroups).map { item => compareObj(item._1, item._2) }
      }

      "fail for an invalid study ID" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy

        val json = makeRequest(GET, uri(study), BAD_REQUEST)
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("invalid study id")
      }

      "fail for an invalid study ID when using an specimen group id" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        val sg = factory.createSpecimenGroup

        val json = makeRequest(GET, uriWithQuery(study, sg), BAD_REQUEST)
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("invalid study id")
      }

      "fail for an invalid specimen group id" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val sg = factory.createSpecimenGroup

        val json = makeRequest(GET, uriWithQuery(study, sg), BAD_REQUEST)
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("specimen group does not exist")
      }

    }

    "POST /studies/sgroups" must {
      "add a specimen group" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val cmdJson = specimenGroupToAddCmd(factory.createSpecimenGroup)
        val json = makeRequest(POST, uri(study), json = cmdJson)

        (json \ "status").as[String] must include ("success")
      }

      "not add a specimen group to enabled study" in new App(fakeApp) {
        doLogin
        addToNonDisabledStudy(
          factory.createDisabledStudy.enable(1, 1) | fail,
          factory.createSpecimenGroup)
      }

      "not add a specimen group to retired study" in new App(fakeApp) {
        doLogin
        addToNonDisabledStudy(
          factory.createDisabledStudy.retire | fail,
          factory.createSpecimenGroup)
      }
    }

    "PUT /studies/sgroups" must {
      "update a specimen group" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val sg = factory.createSpecimenGroup
        specimenGroupRepository.put(sg)

        val sg2 = factory.createSpecimenGroup.copy(id = sg.id)
        val cmdJson = specimenGroupToUpdateCmd(sg2)
        val json = makeRequest(PUT, uri(study, sg2), json = cmdJson)

        (json \ "status").as[String] must include ("success")
      }

      "not update a specimen group on an enabled study" in new App(fakeApp) {
        doLogin
        updateOnNonDisabledStudy(
          factory.createDisabledStudy.enable(1, 1) | fail,
          factory.createSpecimenGroup)
      }

      "not update a specimen group on an retired study" in new App(fakeApp) {
        doLogin
        updateOnNonDisabledStudy(
          factory.createDisabledStudy.retire | fail,
          factory.createSpecimenGroup)
      }

      "not update a specimen group in use by a collection event type" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val sg = factory.createSpecimenGroup
        specimenGroupRepository.put(sg)

        val cet = factory.createCollectionEventType
        val sgData = List(
          factory.createCollectionEventTypeSpecimenGroupData,
          factory.createCollectionEventTypeSpecimenGroupData)
        val cet2 = cet.copy(specimenGroupData = sgData)
        collectionEventTypeRepository.put(cet2);

        val json = makeRequest(
          PUT, uri(study, sg), BAD_REQUEST, json = specimenGroupToUpdateCmd(sg))

        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("specimen group is in use by collection event type")
      }

      "not update a specimen group in use by a specimen link type" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val pt = factory.createProcessingType
        processingTypeRepository.put(pt)

        val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
        specimenLinkTypeRepository.put(slType)
        specimenGroupRepository.put(inputSg)
        specimenGroupRepository.put(outputSg)

        // attempt to update inputSg
        var jsonReply = makeRequest(
          PUT, uri(study, inputSg), BAD_REQUEST, json = specimenGroupToUpdateCmd(inputSg))

        (jsonReply \ "status").as[String] must include ("error")
        (jsonReply \ "message").as[String] must include ("specimen group is in use by specimen link type")

        // attempt to update outputSg
        jsonReply = makeRequest(
          PUT, uri(study, outputSg), BAD_REQUEST, json = specimenGroupToUpdateCmd(outputSg))

        (jsonReply \ "status").as[String] must include ("error")
        (jsonReply \ "message").as[String] must include ("specimen group is in use by specimen link type")
      }

      "not update a specimen group in use by a collection event type and a specimen link type" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val pt = factory.createProcessingType
        processingTypeRepository.put(pt)

        val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
        specimenLinkTypeRepository.put(slType)
        specimenGroupRepository.put(inputSg)
        specimenGroupRepository.put(outputSg)

        // outputSg should be added to collection event
        val cet = factory.createCollectionEventType
        val sgData = List(
          factory.createCollectionEventTypeSpecimenGroupData,
          factory.createCollectionEventTypeSpecimenGroupData)
        val cet2 = cet.copy(specimenGroupData = sgData)
        collectionEventTypeRepository.put(cet2);

        // attempt to update outputSg
        val jsonReply = makeRequest(
          PUT, uri(study, outputSg), BAD_REQUEST, json = specimenGroupToUpdateCmd(outputSg))

        (jsonReply \ "status").as[String] must include ("error")
        (jsonReply \ "message").as[String] must include ("specimen group is in use by collection event type")
        (jsonReply \ "message").as[String] must include ("specimen group is in use by specimen link type")
      }
    }

    "DELETE /studies/sgroups" must {
      "remove a specimen group" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val sg = factory.createSpecimenGroup
        specimenGroupRepository.put(sg)

        val json = makeRequest(DELETE, uri(study, sg, sg.version))

        (json \ "status").as[String] must include ("success")
      }

      "not remove a specimen group from an enabled study" in new App(fakeApp) {
        doLogin
        removeOnNonDisabledStudy(
          factory.createDisabledStudy.enable(1, 1) | fail,
          factory.createSpecimenGroup)
      }

      "not remove a specimen group from an retired study" in new App(fakeApp) {
        doLogin
        removeOnNonDisabledStudy(
          factory.createDisabledStudy.retire | fail,
          factory.createSpecimenGroup)
      }
    }

    "GET /studies/sgroups/inuse" must {
      "reply with specimen group in use" taggedAs(Tag("1")) in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val pt = factory.createProcessingType
        processingTypeRepository.put(pt)

        val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
        specimenLinkTypeRepository.put(slType)
        specimenGroupRepository.put(inputSg)
        specimenGroupRepository.put(outputSg)

        // outputSg should be added to collection event
        val cet = factory.createCollectionEventType
        val sgData = List(
          factory.createCollectionEventTypeSpecimenGroupData,
          factory.createCollectionEventTypeSpecimenGroupData)
        val cet2 = cet.copy(specimenGroupData = sgData)
        collectionEventTypeRepository.put(cet2);

        // attempt to update outputSg
        val jsonReply = makeRequest(GET, uri(study) + "/inuse")

        (jsonReply \ "status").as[String] must include ("success")

        val jsonList = (jsonReply \ "data").as[List[String]]
        jsonList must contain (inputSg.id.id)
        jsonList must contain (outputSg.id.id)
      }
    }

  }

}
