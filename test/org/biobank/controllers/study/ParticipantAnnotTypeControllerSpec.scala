package org.biobank.controllers.study

import org.biobank.fixture._
import org.biobank.domain._
import org.biobank.domain.study.{ Study, StudyId, ParticipantAnnotationType }
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

  //override val annotationTypeName = this.getClass.getName

  override def annotationTypeRepository = TestGlobal.participantAnnotationTypeRepository

  override def createAnnotationType = factory.createParticipantAnnotationType

  override def annotationTypeCopyWithId(at: ParticipantAnnotationType,
                                        id: AnnotationTypeId) =
    at.copy(id = id)

  override def annotationTypeCopyWithStudyId(at: ParticipantAnnotationType,
                                             studyId: StudyId) =
    at.copy(studyId = studyId)

  override def annotationTypeCopyWithVersion(at: ParticipantAnnotationType, version: Long) =
    at.copy(version = version)

  override def annotationTypeCopyWithName(at: ParticipantAnnotationType, name: String) =
    at.copy(name = name)

  override val uriPart = "pannottypes"

  override protected def annotTypeToAddCmdJson(annotType: ParticipantAnnotationType) = {
    super.annotTypeToAddCmdJson(annotType) ++ Json.obj(
      "required"      -> annotType.required
    )
  }

  "Participant Type REST API" when {

    annotationTypeBehaviour

  }

}
