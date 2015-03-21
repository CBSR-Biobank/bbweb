package org.biobank.controllers.study

import org.biobank.fixture._
import org.biobank.domain._
import org.biobank.domain.study.{ Study, StudyId, SpecimenLinkAnnotationType }
import org.biobank.fixture.ControllerFixture
import org.biobank.domain.JsonHelper._

import com.typesafe.plugin._
import org.joda.time.DateTime
import org.scalatest.Tag
import org.scalatestplus.play._
import org.slf4j.LoggerFactory
import play.api.Play.current
import play.api.libs.json._
import play.api.test.Helpers._
import play.api.test.WithApplication

class SpecimenLinkAnnotTypeControllerSpec extends StudyAnnotTypeControllerSpec[SpecimenLinkAnnotationType] {
  import TestGlobal._

  override def annotationTypeRepository = TestGlobal.specimenLinkAnnotationTypeRepository

  override def createAnnotationType = factory.createSpecimenLinkAnnotationType

  override def annotationTypeCopyWithId(at: SpecimenLinkAnnotationType,
                                        id: AnnotationTypeId) =
    at.copy(id = id)

  override def annotationTypeCopyWithStudyId(at: SpecimenLinkAnnotationType,
                                             studyId: StudyId) =
    at.copy(studyId = studyId)

  override def annotationTypeCopyWithVersion(at: SpecimenLinkAnnotationType, version: Long) =
    at.copy(version = version)

  override def annotationTypeCopyWithName(at: SpecimenLinkAnnotationType, name: String) =
    at.copy(name = name)

  override val uriPart: String = "slannottypes"

  "Specimen Link Annotation Type REST API" when {

    annotationTypeBehaviour

  }

}
