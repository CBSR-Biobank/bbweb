package org.biobank.controllers.study

import org.biobank.domain._
import org.biobank.domain.study.{
  Study,
  StudyId,
  ParticipantAnnotationType,
  ParticipantAnnotationTypeRepository }
import org.biobank.domain.study._

import play.api.libs.json._
import org.scalatest.Tag
import org.slf4j.LoggerFactory
import org.joda.time.DateTime
import play.api.Play.current

class ParticipantAnnotTypeControllerSpec
    extends StudyAnnotTypeControllerSpec[ParticipantAnnotationType]  {

  override def annotationTypeRepository =
    app.injector.instanceOf[ParticipantAnnotationTypeRepository]

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
