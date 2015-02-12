package org.biobank.controllers.study

import org.biobank.fixture._
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
import org.scalatestplus.play._

class ParticipantAnnotTypeControllerSpec extends StudyAnnotTypeControllerSpec[ParticipantAnnotationType]  {
  import TestGlobal._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  override protected def uri(study: Study): String = s"/studies/${study.id.id}/pannottypes"

  override protected def annotTypeToAddCmdJson(annotType: ParticipantAnnotationType) = {
    super.annotTypeToAddCmdJson(annotType) ++ Json.obj(
      "required"      -> annotType.required
    )
  }

  private def addOnNonDisabledStudy(study: Study) {
    studyRepository.put(study)

    val annotType = factory.createParticipantAnnotationType.copy(studyId = study.id)
    participantAnnotationTypeRepository.put(annotType)

    val json = makeRequest(POST, uri(study), BAD_REQUEST, annotTypeToAddCmdJson(annotType))

    (json \ "status").as[String] must include ("error")
    (json \ "message").as[String] must include ("is not disabled")
  }

  private def updateOnNonDisabledStudy(study: Study) {
    studyRepository.put(study)

    val annotType = factory.createParticipantAnnotationType.copy(studyId = study.id)
    participantAnnotationTypeRepository.put(annotType)

    val json = makeRequest(PUT, uri(study, annotType), BAD_REQUEST, annotTypeToUpdateCmdJson(annotType))

    (json \ "status").as[String] must include ("error")
    (json \ "message").as[String] must include ("is not disabled")
  }

  def removeOnNonDisabledStudy(study: Study) {
    studyRepository.put(study)

    val sg = factory.createSpecimenGroup.copy(studyId = study.id)
    specimenGroupRepository.put(sg)

    val annotType = factory.createParticipantAnnotationType.copy(studyId = study.id)
    participantAnnotationTypeRepository.put(annotType)

    val json = makeRequest(
      DELETE,
      s"/studies/${annotType.studyId.id}/pannottypes/${annotType.id.id}/${annotType.version}",
      BAD_REQUEST)

    (json \ "status").as[String] must include ("error")
    (json \ "message").as[String] must include ("is not disabled")
  }

  "Participant Type REST API" when {

    "GET /studies/:studyId/pannottypes" must {
      "list none" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val json = makeRequest(GET, uri(study))
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size 0
      }

      "list a single participant annotation type" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = factory.createParticipantAnnotationType
        participantAnnotationTypeRepository.put(annotType)

        val json = makeRequest(GET, uri(study))
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size 1
        compareObj(jsonList(0), annotType)
      }

      "list multiple participant annotation types" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotTypes = List(
          factory.createParticipantAnnotationType,
          factory.createParticipantAnnotationType)
        annotTypes map { annotType => participantAnnotationTypeRepository.put(annotType) }

        val json = makeRequest(GET, uri(study))
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]

        jsonList must have size annotTypes.size
        (jsonList zip annotTypes).map { item => compareObj(item._1, item._2) }
        ()
      }

      "fail for an invalid study ID" in {
        val study = factory.createDisabledStudy

        val json = makeRequest(GET, uri(study), BAD_REQUEST)
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("invalid study id")
      }

      "fail for an invalid study ID when using an annotation type id" in {
        val study = factory.createDisabledStudy
        val annotType = factory.createParticipantAnnotationType

        val json = makeRequest(GET, uriWithQuery(study, annotType), BAD_REQUEST)
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("invalid study id")
      }

      "fail for an invalid participant annotation type id" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = factory.createParticipantAnnotationType

        val json = makeRequest(GET, uriWithQuery(study, annotType), NOT_FOUND)
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("annotation type does not exist")
      }

    }

    "POST /studies/:studyId/pannottypes" must {
      "add a participant annotation type" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = factory.createParticipantAnnotationType
        val json = makeRequest(
          POST,
          uri(study),
          json = annotTypeToAddCmdJson(annotType))
        (json \ "status").as[String] must include ("success")
      }

      "not add a participant annotation type to an enabled study" in {
        addOnNonDisabledStudy(factory.createEnabledStudy)
      }

      "not add a participant annotation type to an retired study" in {
        addOnNonDisabledStudy(factory.createRetiredStudy)
      }

      "fail when adding and study IDs do not match" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = factory.createParticipantAnnotationType

        val study2 = factory.createDisabledStudy

        val json = makeRequest(POST,
                               uri(study2),
                               BAD_REQUEST,
                               json = annotTypeToAddCmdJson(annotType))
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("study id mismatch")
      }

    }

    "PUT /studies/:studyId/pannottypes" must {
      "update a participant annotation type" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = factory.createParticipantAnnotationType
        participantAnnotationTypeRepository.put(annotType)

        val annotType2 = factory.createParticipantAnnotationType.copy(
          id = annotType.id,
          version = annotType.version
        )

        val json = makeRequest(PUT, uri(study, annotType2), json = annotTypeToUpdateCmdJson(annotType2))

        (json \ "status").as[String] must include ("success")
      }

      "not update a participant annotation type on an enabled study" in {
        updateOnNonDisabledStudy(factory.createEnabledStudy)
      }

      "not update a participant annotation type on an retired study" in {
        updateOnNonDisabledStudy(factory.createRetiredStudy)
      }

      "fail when updating and study IDs do not match" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = factory.createParticipantAnnotationType
        participantAnnotationTypeRepository.put(annotType)

        val annotType2 = factory.createParticipantAnnotationType.copy(
          id = annotType.id,
          version = annotType.version
        )

        val study2 = factory.createDisabledStudy

        val json = makeRequest(PUT,
                               uri(study2, annotType2),
                               BAD_REQUEST,
                               json = annotTypeToUpdateCmdJson(annotType2))

        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("study id mismatch")
      }

      "fail when updating and annotation type IDs do not match" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = factory.createParticipantAnnotationType
        participantAnnotationTypeRepository.put(annotType)

        val annotType2 = factory.createParticipantAnnotationType.copy(
          id = annotType.id,
          version = annotType.version
        )

        val annotType3 = factory.createParticipantAnnotationType

        val json = makeRequest(PUT,
                               uri(study, annotType3),
                               BAD_REQUEST,
                               json = annotTypeToUpdateCmdJson(annotType2))

        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("annotation type id mismatch")
      }
    }

    "DELETE /studies/pannottype" must {
      "remove a participant annotation type" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = factory.createParticipantAnnotationType
        participantAnnotationTypeRepository.put(annotType)

        val json = makeRequest(DELETE, uri(study, annotType, annotType.version))

        (json \ "status").as[String] must include ("success")
      }

      "not remove a participant annotation type on an enabled study" in {
        removeOnNonDisabledStudy(factory.createEnabledStudy)
      }

      "not remove a participant annotation type on a retired study" in {
        removeOnNonDisabledStudy(factory.createRetiredStudy)
      }
    }
  }

}
